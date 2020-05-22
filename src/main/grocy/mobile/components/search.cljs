(ns grocy.mobile.components.search)

(defn search-bar
  []
  [:input {:on-change #(rf/dispatch [:search "foo"])}])