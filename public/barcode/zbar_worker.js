if ('importScripts' in self) {
  // Import the emscripten'd file that loads the wasm.
  importScripts(`/barcode/zbar.js`);
}

const utf8BufferToString = (buffer, addr) => {
  let end = addr;
  while (buffer[end]) {
    ++end;
  }
  const str = new Uint8Array(buffer.slice(addr, end));
  const encodedString = String.fromCharCode.apply(null, str);
  const decodedString = decodeURIComponent(escape(encodedString));
  return decodedString;
};

const Scanner = mixin => {
  const mod = Module(mixin);
  const api = {
    createBuffer: mod.cwrap('createBuffer', 'number', ['number']),
    deleteBuffer: mod.cwrap('deleteBuffer', '', ['number']),
    scanQrcode: mod.cwrap('scanQrcode', 'number', [
      'number',
      'number',
      'number'
    ]),
    getScanResults: mod.cwrap('getScanResults', 'number', [])
  };
  const scanner = {
    scanQrcode: (imgData, width, height) => {
      const buf = api.createBuffer(width * height * 4);
      mod.HEAP8.set(imgData, buf);
      const results = [];
      if (api.scanQrcode(buf, width, height)) {

        const res_addr = api.getScanResults();
        results.push(utf8BufferToString(mod.HEAP8, res_addr));
        api.deleteBuffer(res_addr);
      }
      return results;
    }
  };
  return new Promise((resolv, reject) => {
    mod.then(() => {
      resolv(scanner);
    });
  });
};

var scanner;
var zone = {};

const loadImage = src => {
  // turn data from ImageBitmap to ImageData
  let width  = src.width  * zone.w
  let height = src.height * zone.h
  let sx     = src.width  * zone.x
  let sy     = src.height * zone.y
  let offscreen = new OffscreenCanvas(width, height);
  let ctx = offscreen.getContext("2d");
  ctx.drawImage(src, sx, sy, width, height, 0, 0, width, height);
  return {
    imageData: ctx.getImageData(0, 0, width, height).data,
    width,
    height,
  }
} 

self.onmessage = async e => {
    // Initializing.
    const data = e.data
    if(data.init){
      scanner = await Scanner({locateFile: file => ('/barcode/' + file)});
      console.log('loaded scanner');
      zone = {w: data['w-pct'], h: data['h-pct'], x: data['left-pct'], y: data['top-pct']};
      return;
    }
    const { imageData, width, height } = loadImage(data);
    const res = scanner.scanQrcode(imageData, width, height)
    self.postMessage(res);
};