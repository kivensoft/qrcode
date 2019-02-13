# 轻量级二维码生成库
轻量级二维码生成库, 从zxing中剥离生成代码, 只能生成二维码, 无法解析二维码.

使用方法:
```
  QRCode q = new QRCode("测试文本", 200, 200);
  ByteBuffer bb = q.toButeBuffer("png");
  FileOutputStream fos = new FileOutputStream("c:/1.png");
  fos.write(bb.array());
  fos.close();
```