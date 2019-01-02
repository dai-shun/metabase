(ns metabase.cmd.serialization
  (:require [metabase.db :as mdb]
            [metabase.models
             [card :refer [Card]]
             [collection :refer [Collection]]
             [dashboard :refer [Dashboard]]
             [database :refer [Database]]
             [field :refer [Field] :as field]
             [metric :refer [Metric]]
             [pulse :refer [Pulse]]
             [segment :refer [Segment]]
             [table :refer [Table]]]
            [metabase.serialization
             [dump :as dump]
             [load :as load]]
            [schema.core :as s]
            [toucan.db :as db]
            [metabase.util
             [i18n :refer [trs]]
             [schema :as su]])
  (:refer-clojure :exclude [load]))

(def ^:private Mode
  (su/with-api-error-message (s/enum :skip :update)
    (trs "invalid mode value")))

(s/defn load
  "Load serialized metabase instance as created by `dump` command from directory `path`."
  [mode :- Mode, path]
  (mdb/setup-db-if-needed!)
  (let [context {:mode mode}]
    (load/load path context Database)
    (load/load path context Collection)
    (load/load-settings path context)
    (load/load-dependencies path context)))

(defn- dump-all
  [path entities]
  (doseq [e entities]
    (dump/dump path e)))

(defn dump
  "Serialized metabase instance into directory `path`."
  [path]
  (mdb/setup-db-if-needed!)
  (dump-all path (Database))
  (dump-all path (Table))
  (dump-all path (field/with-values (Field)))
  (dump-all path (Metric))
  (dump-all path (Segment))
  (dump-all path (db/select Collection :personal_owner_id nil))
  (dump-all path (Card))
  (dump-all path (Dashboard))
  (dump-all path (Pulse))
  (dump/dump-settings path)
  (dump/dump-dependencies path)
  (dump/dump-dimensions path))