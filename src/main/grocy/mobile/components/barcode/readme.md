Barcode scanner
===============

The barcode scanner module leverages some technologies not fully integrated into the clojurescript universe just yet (`wasm`). 

As such, the actual logic for doing the work is in a `.wasm` file in public/barcode along with some js "glue" code and a web-worker. 

The web-worker is initialized from the component, and then communicates by message. 
The component sends an image frame, the `wasm` scans it for a barcode and sends back the first one it finds or `[]`.

The benefits of this approach (web-worker + wasm) are two-fold:
1. I dont have to faff about with cljs->js interop when dealing with the wasm code and
2. All the hard work of barcode scanning happens off the main thread, keeping the ui buttery smooth. 

In terms of performance, scanning a subset of a `1920x1080` frame takes about 100ms, however, this can vary. As such, don't attempt to scan every frame, or even every `nth` frame. Rather, scan a frame, and when you get a response, scan another frame.

TODO:
If something goes wrong, you'll need to start scanning agian..