;; The memory used for the heap
;; Here the 1 means we need at least one 1 page (64 KiB)
(memory (export "memory") 1)

;; Used to track the next available heap address
(global $next_free_space (mut i32) (i32.const 0))

;; Used as a temporary value when allocating objects
(global $temp_ref (mut i32) (i32.const 0))
