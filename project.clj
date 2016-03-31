(defproject stars "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [devcards "0.2.1-6"]
                 [datascript "0.15.0"]
                 [sablono "0.5.3"]
                 [cljsjs/react-dom-server "0.14.3-0"]
                 [org.omcljs/om "1.0.0-alpha32"]
                 [figwheel-sidecar "0.5.0-3"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [org.webjars/bootstrap "4.0.0-alpha.2"]
                 [org.webjars/font-awesome "4.5.0"]]

  :plugins [[lein-cljsbuild "1.1.1"]]

  :source-paths ["src" "script"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src"]

                :figwheel     {:on-jsload "stars.app/on-js-reload"}

                :compiler     {:main                 stars.app
                               :asset-path           "js/compiled/out"
                               :output-to            "resources/public/js/compiled/stars.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true}}
               {:id           "devcards"
                :source-paths ["src"]
                :figwheel     {:devcards true}
                :compiler     {:main                 stars.app
                               :asset-path           "js/compiled/devcards_out"
                               :output-to            "resources/public/js/compiled/stars_devcards.js"
                               :output-dir           "resources/public/js/compiled/devcards_out"
                               :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id           "min"
                :source-paths ["src"]
                :compiler     {:output-to     "resources/public/js/compiled/stars.js"
                               :main          stars.app
                               :optimizations :advanced
                               :pretty-print  false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             :ring-handler server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             :open-file-command "open-with-intellij"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
