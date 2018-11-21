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
			imageFilename = args[1];
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
			
			System.out.println("Extracted message is:");
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
			}
			File outputFile = new File(outputFilename);
			try {
				System.out.println("Writing output file");
				ImageIO.write(outputImage, "PNG", outputFile);
			} catch (IOException e) {
				System.out.println("Failed writing the output image to \"" + outputFilename + "\"");
			}
			System.out.println("Done!");
		}
	}
	
	private static BufferedImage encode( BufferedImage imIn, String message ) {
		BufferedImage imOut = imIn;
		byte[] bytes = message.getBytes();
		if( (bytes.length + 1) * 8 > imOut.getHeight() * imOut.getWidth() * 3 ) {
			return null;
		}
		
		BitSet bits = new BitSet(imOut.getHeight() * imOut.getWidth() * 3);
		bits.clear(0, (imOut.getHeight() * imOut.getWidth() * 3)-1);
		bits.or(BitSet.valueOf(bytes));
		
		int bIndex = 0;
		for( int y = 0; y < imOut.getHeight(); y++ ) {
			for( int x = 0; x < imOut.getWidth(); x++ ) {
				int pixel = imOut.getRGB(x, y);
				
				int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = (pixel) & 0xff;
				
				r = (r-(r%2)) + (bits.get(bIndex++) ? 1 : 0);
				g = (g-(g%2)) + (bits.get(bIndex++) ? 1 : 0);
				b = (b-(b%2)) + (bits.get(bIndex++) ? 1 : 0);
				
				pixel = (a << 24) | (r << 16) | (g << 8) | b;
				
				imOut.setRGB(x, y, pixel);
			}
		}
		
		return imOut;
	}
	
	private static String decode( BufferedImage image ) {
		String message = "";
		
		return message;
	}
}
