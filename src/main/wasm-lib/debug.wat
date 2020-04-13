(export "run_gc" (func $gc))
(export "request_more_memory" (func $request_more_memory))
(func $silentLog (param i32) (result i32)
    local.get 0
    call $log
    local.get 0
)
(func $getStackBase (result i32)
    global.get $stack_base
)
(export "getStackBase" (func $getStackBase))

(func $getStackFrameStart (result i32)
    global.get $stack_frame_start
)
(export "getStackFrameStart" (func $getStackFrameStart))
(func $setStackFrameStart (param $address i32)
    local.get $address
    global.set $stack_frame_start
)
(export "setStackFrameStart" (func $setStackFrameStart))

(func $getStackPointer (result i32)
    global.get $stack_pointer
)
(export "getStackPointer" (func $getStackPointer))
(func $setStackPointer (param $address i32)
    local.get $address
    global.set $stack_pointer
)
(export "setStackPointer" (func $setStackPointer))
(func $readWord (param i32) (result i32)
    local.get 0
    i32.load
)
(export "readWord" (func $readWord))
(func $getHeapLastAllocated (result i32)
    global.get $heap_last_allocated
)
(export "getHeapLastAllocated" (func $getHeapLastAllocated))
