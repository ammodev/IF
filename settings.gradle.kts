rootProject.name = "IF-parent"

include("IF")
include("adventure-support")

include("inventory-view:iv-interface")
include("inventory-view:iv-abstract-class")
include("inventory-view:iv-abstraction")

// NMS
include(":nms:abstraction")

include(":nms:1_20_0")
include(":nms:1_20_1")
include(":nms:1_20_2")
include(":nms:1_20_3-4")
include(":nms:1_20_6")

include(":nms:1_21_0")
include(":nms:1_21_1")
include(":nms:1_21_2-3")
