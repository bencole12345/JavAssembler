;; Allocates a requested amount of heap space and returns
;; the address that was allocated.
(func $alloc (param $size i32) (result i32)
  (local $allocatedAddress i32)

  ;; Read the current next free heap address
  global.get $nextFreeSpace
  local.set $allocatedAddress

  ;; Update the next free address
  global.get $nextFreeSpace
  local.get $size
  i32.add
  global.set $nextFreeSpace

  ;; Return the allocated address
  local.get $allocatedAddress
)
