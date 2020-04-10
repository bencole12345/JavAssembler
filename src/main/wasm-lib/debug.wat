(export "run_gc" (func $gc))

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
