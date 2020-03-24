;; The memory used for the heap
;; Here the 1 means we need at least one 1 page (64 KiB)
(memory (export "memory") 1)

;; Used to track the next available heap address
(global $nextFreeSpace (mut i32) (i32.const 0))

;; Used when duplicating references on the stack
(global $tempRef (mut i32) (i32.const 0))
