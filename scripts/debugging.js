const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();

const importObject = {
  console: {
    log: function(arg) {
      console.log(arg);
    }
  }
}

const dumpObject = (address, recurse, wasmInstance) => {
  const addressHex = "0x" + address.toString(16).toUpperCase();
  const headers = wasmInstance.readWord(address) & 0xff;
  const headersDecoded = {
    isObject: Boolean(headers & 0x01),
    gcFlag: Boolean(headers & 0x02)
  };
  const sizeField = wasmInstance.readWord(address + 1);
  const vtable = wasmInstance.readWord(address + 5);
  let attributes = [];
  for (let i = 0; i < sizeField; i += 4) {
    const attribute = wasmInstance.readWord(address + 9 + i);
    const dumped = recurse && wasmInstance._gc_is_pointer(address, i)
            ? makeDumpFunction(wasmInstance)(attribute) : attribute;
    attributes.push(dumped);
  }
  return { address, addressHex, headersDecoded, sizeField, vtable, attributes };
}

const dumpArray = (address, recurse, wasmInstance) => {
  const addressHex = "0x" + address.toString(16).toUpperCase();
  const headers = wasmInstance.readWord(address) & 0xff;
  const headersDecoded = {
    isObject: Boolean(headers & 0x01),
    gcFlag: Boolean(headers & 0x02),
    containsPointers: Boolean(headers & 0x04)
  };
  const sizeField = wasmInstance.readWord(address + 1);
  let elements = [];
  for (let i = 0; i < sizeField; i += 4) {
    const element = wasmInstance.readWord(address + 5 + i);
    const dumped = recurse && headersDecoded.containsPointers
            ? makeDumpFunction(wasmInstance)(element) : element;
    elements.push(dumped);
  }
  return { address, addressHex, headersDecoded, sizeField, elements };
}

const makeDumpFunction = (wasmInstance) => (address, recurse=false) => {
  const isObj = wasmInstance.readWord(address) & 0x01;
  return isObj ? dumpObject(address, recurse, wasmInstance) : dumpArray(address, recurse, wasmInstance);
}

const debug = async (makeDumpFunction) => {
  const watPath = path.resolve(__dirname, '.', 'sample_programs_compiled', 'tests.wat');
  const watBuffer = fs.readFileSync(watPath, 'utf8');
  const wasmModule = wabt.parseWat(watPath, watBuffer);
  const { buffer } = wasmModule.toBinary({});
  const module = await WebAssembly.compile(buffer);
  const instance = await WebAssembly.instantiate(module, importObject);
  let wasmInstance = instance.exports;

  const dump = makeDumpFunction(wasmInstance);

  // Set a breakpoint here
  let i = 1;
}

debug(makeDumpFunction);
