package cn.kivensoft.qrcode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import cn.kivensoft.qrcode.zxing.ByteMatrix;
import cn.kivensoft.qrcode.zxing.EncodeHintType;
import cn.kivensoft.qrcode.zxing.Encoder;
import cn.kivensoft.qrcode.zxing.ErrorCorrectionLevel;
import cn.kivensoft.qrcode.zxing.ZXingQRCode;

/**
 * A component for encoding values into QR coded images and embedding them into
 * Vaadin applications.
 * 
 * @author John Ahlroos (www.jasoft.fi)
 */
public class QRCode {
	public static final String PNG = "png";
	public static final String jpg = "jpg";

	private final ZXingQRCode qrcode = new ZXingQRCode();

	private int pixelWidth = 200;
	private int pixelHeight = 200;

	private Color fgColor = Color.BLACK;
	private Color bgColor = Color.WHITE;

	private String value;

	private ErrorCorrectionLevel ecl = ErrorCorrectionLevel.L;

	/**
	 * Constructs an empty <code>QRCode</code> with no caption.
	 */
	public QRCode() {
	}

	public QRCode(String value, int width, int height) {
		this.value = value;
		this.pixelWidth = width;
		this.pixelHeight = height;
	}

	public ByteBuffer toByteBuffer(String imageType) throws Exception {
		BufferedImage image = toBufferedImage();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, imageType, output);
		return ByteBuffer.wrap(output.toByteArray());
	}

	public BufferedImage toBufferedImage() throws Exception {
		if (pixelHeight < 0 || pixelWidth < 0)
			throw new Exception("pixelHeight or pixelWidth less than 0");

		String value = getValue();
		if (value == null) value = "";

		HashMap<EncodeHintType, String> hints = new HashMap<>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		Encoder.encode(value, ecl, hints, qrcode);
		ByteMatrix matrix = renderResult(qrcode, pixelWidth, pixelHeight);
		return toBufferedImage(matrix, fgColor.getRGB(), bgColor.getRGB());
	}

	/**
	 * Copy&Paste from com.google.zxing.qrcode.QRCodeWriter.java
	 * 
	 * http://zxing.googlecode.com/svn-history/r800/trunk/core/src/com/google/
	 * zxing/qrcode/QRCodeWriter.java
	 * 
	 */
	private static final int QUIET_ZONE_SIZE = 4;

	private static ByteMatrix renderResult(ZXingQRCode code, int width, int height) {
		ByteMatrix input = code.getMatrix();
		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int qrWidth = inputWidth + (QUIET_ZONE_SIZE << 1);
		int qrHeight = inputHeight + (QUIET_ZONE_SIZE << 1);
		int outputWidth = Math.max(width, qrWidth);
		int outputHeight = Math.max(height, qrHeight);

		int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
		int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
		int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

		ByteMatrix output = new ByteMatrix(outputWidth, outputHeight);
		byte[][] outputArray = output.getArray();
		byte[] row = new byte[outputWidth];

		// 1. Write the white lines at the top
		for (int y = 0; y < topPadding; y++) {
			setRowColor(outputArray[y], (byte) 255);
		}

		// 2. Expand the QR image to the multiple
		byte[][] inputArray = input.getArray();
		for (int y = 0; y < inputHeight; y++) {
			// a. Write the white pixels at the left of each row
			for (int x = 0; x < leftPadding; x++) {
				row[x] = (byte) 255;
			}

			// b. Write the contents of this row of the barcode
			int offset = leftPadding;
			for (int x = 0; x < inputWidth; x++) {
				byte value = (inputArray[y][x] == 1) ? 0 : (byte) 255;
				for (int z = 0; z < multiple; z++) {
					row[offset + z] = value;
				}
				offset += multiple;
			}

			// c. Write the white pixels at the right of each row
			offset = leftPadding + (inputWidth * multiple);
			for (int x = offset; x < outputWidth; x++) {
				row[x] = (byte) 255;
			}

			// d. Write the completed row multiple times
			offset = topPadding + (y * multiple);
			for (int z = 0; z < multiple; z++) {
				System.arraycopy(row, 0, outputArray[offset + z], 0,
						outputWidth);
			}
		}

		// 3. Write the white lines at the bottom
		int offset = topPadding + (inputHeight * multiple);
		for (int y = offset; y < outputHeight; y++) {
			setRowColor(outputArray[y], (byte) 255);
		}

		return output;
	}

	/**
	 * Copy & Paste from com.google.zxing.qrcode.QRCodeWriter.java
	 * 
	 * http://zxing.googlecode.com/svn-history/r800/trunk/core/src/com/google/
	 * zxing/qrcode/QRCodeWriter.java
	 * 
	 */
	private static void setRowColor(byte[] row, byte value) {
		for (int x = 0; x < row.length; x++) {
			row[x] = value;
		}
	}

	/**
	 * Copy & Paste from com.google.zxing.client.j2se.MatrixToImageWriter.java
	 * 
	 * http://zxing.googlecode.com/svn-history/r1028/trunk/javase/src/com/google
	 * /zxing/client/j2se/MatrixToImageWriter.java
	 */
	protected static BufferedImage toBufferedImage(ByteMatrix matrix,
			int fgColor, int bgColor) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) == 0 ? fgColor : bgColor);
			}
		}
		return image;
	}

	public int getPixelWidth() {
		return pixelWidth;
	}

	public void setPixelWidth(int pixelWidth) {
		this.pixelWidth = pixelWidth;
	}

	public int getPixelHeight() {
		return pixelHeight;
	}

	public void setPixelHeight(int pixelHeight) {
		this.pixelHeight = pixelHeight;
	}

	public Color getFgColor() {
		return fgColor;
	}

	public void setFgColor(Color fgColor) {
		this.fgColor = fgColor;
	}

	public Color getBgColor() {
		return bgColor;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ErrorCorrectionLevel getEcl() {
		return ecl;
	}

	public void setEcl(ErrorCorrectionLevel ecl) {
		this.ecl = ecl;
	}

}
