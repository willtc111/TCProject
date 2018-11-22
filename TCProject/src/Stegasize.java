import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;

import javax.imageio.ImageIO;

public class Stegasize {

	public static void main(String[] args) {
		String imageFilename = null,messageFilename = null,outputFilename = null;
		if( args.length == 0 ) {
			System.out.println("Using test image/message");
			imageFilename = "defaultImage.png";
			messageFilename = "defaultMessage";
			outputFilename = "defaultOutput.png";
		} else if(args.length == 1 ) {
			imageFilename = args[0];
		} else if( args.length == 2 ) {
			imageFilename = args[0];
			messageFilename = args[1];
			outputFilename = "m"+imageFilename;
		} else if( args.length == 3 ) {
			imageFilename = args[0];
			messageFilename = args[1];
			outputFilename = args[2];
		} else {
			System.out.println("Usage: \"Stagasize image_filename message_filename [output_filename]\"");
			System.exit(0);
		}
		
		File iFile = new File( imageFilename );
		BufferedImage image = null;
		try {
			image = ImageIO.read(iFile);
		} catch (IOException e1) {
			System.out.println("Failed reading the image file: \"" + imageFilename + "\".");
			System.exit(1);
		}
		
		if( args.length == 1 ) {
			// Decoding
			
			String message = decode(image);
			
			System.out.println(message);
			
		} else {
			// Encoding

			String message = null;
			try {
				message = new String(Files.readAllBytes(Paths.get(messageFilename)), Charset.defaultCharset());
			} catch (IOException e) {
				System.out.println("Failed reading the message file: \"" + messageFilename + "\".");
				e.printStackTrace();
				System.exit(1);
			}
			
			BufferedImage outputImage = encode(image, message);
			if( outputImage == null ) {
				System.out.println("Unable to fit message within image.  Use larger image or smaller message.");
				System.exit(0);
			}
			File outputFile = new File(outputFilename);
			try {
				System.out.println("Writing output file");
				ImageIO.write(outputImage, "PNG", outputFile);
			} catch (IOException e) {
				System.out.println("Failed writing the output image to \"" + outputFilename + "\"");
				System.exit(1);
			}
			System.out.println("Done!");
		}
	}
	
	private static BufferedImage encode( BufferedImage im, String message ) {
		byte[] bytes = message.getBytes();
		int numBits = im.getHeight() * im.getWidth() * 3;
		if( (bytes.length + 1) * 8 > numBits ) {
			return null;
		}
		
		BitSet bits = new BitSet(numBits);
		bits.clear(0, (im.getHeight() * im.getWidth() * 3) - 1);
		bits.or(BitSet.valueOf(bytes));
		
		int bIndex = 0;
		for( int y = 0; y < im.getHeight(); y++ ) {
			for( int x = 0; x < im.getWidth(); x++ ) {
				int pixel = im.getRGB(x, y);
				
				int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = (pixel) & 0xff;
				
				r = (r-(r%2)) + (bits.get(bIndex++) ? 1 : 0);
				g = (g-(g%2)) + (bits.get(bIndex++) ? 1 : 0);
				b = (b-(b%2)) + (bits.get(bIndex++) ? 1 : 0);
				
				pixel = (a << 24) | (r << 16) | (g << 8) | b;
				
				im.setRGB(x, y, pixel);
			}
		}
		return im;
	}
	
	private static String decode( BufferedImage image ) {
		BitSet bits = new BitSet(image.getHeight() * image.getWidth() * 3);
		
		int bIndex = 0;
		for( int y = 0; y < image.getHeight(); y++ ) {
			for( int x = 0; x < image.getWidth(); x++ ) {
				int pixel = image.getRGB(x, y);
				
				//int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = (pixel) & 0xff;
				
				bits.set(bIndex++, r % 2 == 1);
				bits.set(bIndex++, g % 2 == 1);
				bits.set(bIndex++, b % 2 == 1);
			}
		}
		
		return new String(bits.toByteArray(), Charset.defaultCharset());
	}
}
