PNG library
===========

Introduction
------------

This is a modern Java library for decoding and encoding PNG image files. All chunk types and color modes are supported. Example usage:

```java
// Encoding
var img = new BufferedRgbaImage(...);
img.setPixel(...);
PngImage png = ImageEncoder.encode(img);
png.beforeIdats.add(new Gama(1 / 2.2));
png.afterIdats.add(new Text("Author", "Myself"));
png.write(new File("output.png"));

// Decoding
PngImage png = PngImage.read(new File("input.png"));
for (Chunk chunk : png.beforeIdats)
    print(chunk);
var img = (GrayImage)ImageDecoder.decode(png);
for (int y = 0; y < img.getHeight(); y++) {
    for (int x = 0; x < img.getWidth(); x++) {
        draw(img.getPixel(x, y));
    }
}
```

What makes this library modern? It:

* was started in the year 2022 with all the benefits of hindsight;
* presents an easy-to-use, type-safe, misuse-resistant API;
* emphasizes implementation correctness and security over sprawling features and fast code;
* uses recent Java-language features to make the code more concise and readable;
* consumes more CPU and memory to simplify the logic and improve reliability.


Features
--------

* Decode RGB, grayscale, and paletted images, without or without alpha channel, of all bit depths, with all filter types, with or without interlacing
* Encode RGB, grayscale, and paletted images, without or without alpha channel, of all bit depths, with filter type 0, with or without interlacing
* Up-convert images with bit depths that are not 1/2/4/8/16 (e.g. RGBA 5.6.5.4 to 8.8.8.8)
* Parse, represent, interpret, and serialize all the known chunk types for the PNG standard, extension, and APNG
* Handle huge chunks up to the standard's size limit (2^31 − 1 bytes)
* Store and convey all unknown chunk types
* Treat MNG and JNG files as entirely composed of custom chunks
* Concise and relatively safe API where most objects are immutable
* Compact, modular, auditable implementation at ~5400 lines of code
* Strictly check out-of-range values, checksum mismatches, reading/writing more/less than the expected data length, arithmetic overflow, length overflow
* Have an extensive test suite for image encode-decode round trip and every chunk type

Outside of scope:
* Drawing, filtering, resampling, color space conversion, and other image effects
* Lossy color reduction, palette quantization, and dithering
* Minimizing data size by manipulating row filters and DEFLATE compression
* Streaming chunks, rows, and pixels instead of buffering everything in memory


Examples
--------

Sample program code and output images can be found on the project home page: https://www.nayuki.io/page/png-library#examples


Code overview
-------------

### `XngFile` class
This low-level class reads and writes PNG/MNG/JNG files, handles chunk boundaries and checksums, and optionally parses known PNG chunk types. Most users don’t need to use this.

### `PngImage` class
This mid-level class is like `XngFile` but only works with PNG files and imposes some constraints on chunk ordering and expected chunks. This does not deal with raw pixel data.

### Chunk and subtypes
These represent chunks in memory. Reading bytes can produce `Chunk` objects, and these objects can be written to bytes. The raw bytes that comprise chunk fields are interpreted as `int`, `String`, `enum`, etc. to the maximum extent possible.

### Random-access image types
The interface `RgbaImage` represents an image where any pixel can be retrieved or computed quickly. The class `BufferedRgbaImage` is backed by an array so that you can get or set any pixel. There are analogous types for grayscale images.

### ImageDecoder, ImageEncoder
These translate between `PngImage` objects (with chunks and compressed bytes) and types like `RgbaImage` (raw pixel arrays).

### No `null`s
All function arguments, return values, and object fields must not be `null`. Users of this library must not pass in `null` values, and in turn, the library will not return `null` values. The optionality of a value is instead conveyed by `java.util.Optional`. The library might use `null` internally within functions, but does not expose these values to user code.

### Immutability
Objects of any chunk type included in this library must be treated as immutable. All their fields are private. Due to `record`, each field implicitly generates a getter method with the same name. There are no setter methods. If a chunk type is composed entirely of immutable fields (e.g. `int`, `String`), then it is truly immutable. Otherwise, a chunk type might choose to return its internal `byte[]` directly to avoid the cost of making defensive copies, but this means immutability cannot be enforced.

`XngFile` objects should be treated as immutable, but their `List<Chunk>` and the chunks themselves might not be able to enforce immutability.

For all immutable types, all data values are checked strictly at the time of object construction.

These objects are mutable: `PngImage`, `BufferedRgbaImage`, `BufferedGrayImage`. The validity of input data is checked at idiosyncratic occasions.

### Access control
Every class, interface, enumeration, record, method, and field is marked with the proper access modifier such as public or private. This is the standard practice in Java programming, and is hardly special if it wasn’t for other libraries making mistakes in this aspect.

### Lossless chunks
All the included chunk types support lossless round-tripping, where reading bytes into chunk objects and writing them out will produce exactly the same bytes. This means, for example, that any compressed data must be stored in memory because decompressing and recompressing can produce different results. Furthermore, `XngFile` and `PngFile` preserve the order of chunks without forcing a canonical representation.

### Assuming abundant memory
For the sake of reducing conceptual complexity and improving reliability, this library makes design trade-offs that increase memory usage. This is possible because memory is much cheaper now than when the PNG format was first released, but correctness and security vulnerabilities became bigger concerns.

The included in-memory image formats all use 16 bits per channel, even when handling images with lower bit depths like 8. This increases generality and decreases special cases at the cost of using more memory.

There is no support for streaming chunks or pixels; most operations are one-shot. For example, `ImageDecoder.decode()` takes a `PngImage` object containing all the chunks in memory, and yields a `BufferedRgbaImage` object containing all the pixels in memory. The lack of streaming dramatically simplifies the API, reduces the implementation logic and error checks, and minimizes the chances of errors in both the library code and user code.

### Default concurrency
The codebase essentially doesn’t deal with concurrency. There is no global mutable state. Static functions are reentrant, so they can be called from multiple threads simultaneously. Functions and methods are structured around call-and-return without unbounded waits (except for I/O). The code has no considerations for situations where two or more threads use mutable objects. There is no locking, inter-thread communication, waiting for actions from other threads, etc. Sharing mutable objects safely requires the user’s code to have proper locking or transfers. The library may choose in the future to implement fork-join for intensive calculations, but these private threads have no visible effect to the user.


License
-------

Copyright © 2023 Project Nayuki. (MIT License)  
[https://www.nayuki.io/page/png-library](https://www.nayuki.io/page/png-library)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

* The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

* The Software is provided "as is", without warranty of any kind, express or
  implied, including but not limited to the warranties of merchantability,
  fitness for a particular purpose and noninfringement. In no event shall the
  authors or copyright holders be liable for any claim, damages or other
  liability, whether in an action of contract, tort or otherwise, arising from,
  out of or in connection with the Software or the use or other dealings in the
  Software.
