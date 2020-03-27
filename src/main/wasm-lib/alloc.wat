;; Allocates a requested amount of heap space and returns
;; the address that was allocated.
(func $alloc
  
  ;; The number of bytes to allocate
  (param $size i32)

  ;; Returns the address that was allocated
  (result i32)

  ;; Used to track the address that was allocated
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


;; Allocates the requested amount of heap space and sets the
;; vtable pointer. Returns the allocated address.
(func $alloc_and_set_vtable

  ;; Number of bytes to allocate
  (param $size i32)

  ;; The vtable value to set
  (param $vtableAddress i32)

  ;; Returns the address that was allocated
  (result i32)

  ;; Used to track the address that was allocated
  (local $allocatedAddress i32)

  ;; Read the current next free heap address
  global.get $nextFreeSpace
  local.set $allocatedAddress

  ;; Update the next free address
  global.get $nextFreeSpace
  local.get $size
  i32.add
  global.set $nextFreeSpace

  ;; Set the vtable pointer
  local.get $allocatedAddress
  local.get $vtableAddress
  i32.store

  ;; Return the allocated address
  local.get $allocatedAddress
)


;; Resets the memory allocator
(func $reset_allocator
  i32.const 0
  global.set $nextFreeSpace
)
(export "reset_allocator" (func $reset_allocator))
