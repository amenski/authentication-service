package it.aman.authenticationservice.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import it.aman.authenticationservice.config.exception.AuthException;
import it.aman.authenticationservice.config.exception.AuthExceptionEnums;

public final class FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	
	private FileUtils(){
		//
	}
	
	/**
	 * Check if the file is of permitted extension and return it
	 * 
	 * @param file
	 * @return file extension
	 * @throws EthException
	 */
	public static String validateFileType(MultipartFile file, String[] permittedFileTypes) throws AuthException{
		String methodName = "validateFileType()";
		logger.info("{} filename: [{}] and filetypes [{}]", methodName,file.getOriginalFilename(), permittedFileTypes);
		Assert.notNull(file, "Uploaded file can not be null");
		Set<String> fileTypeSet = new HashSet<>();
		String fileExtension = "";
		try {
			fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());

			Collections.addAll(fileTypeSet, permittedFileTypes);

			if (!fileTypeSet.contains(fileExtension) || file.isEmpty())
				throw AuthExceptionEnums.WRONG_FILE_TYPE_EXCEPTION.get();

		}catch (Exception e) {
			logger.error(AuthConstants.PARAMETER_2, methodName, e.getMessage());
			throw e;
		}
		return fileExtension;
	}
	
	/**
	 * Upload file to a path.
	 * File name will hold the form [filename + UserName + date]
	 * 
	 * @param fullFileName
	 * @param uploadDir
	 * @param filecontent
	 * @return Path
	 * @throws IOException
	 * @throws EthException
	 */
	public static Path uploadFileToPath(String fullFileName, String uploadDir, byte[] filecontent) throws IOException, AuthException{
		String methodName = "uploadFileToPath()";
		Assert.notNull(fullFileName, "Filename should not be null.");
		Assert.notNull(filecontent, "Filecontent can not be null.");
		
		Path fileAbsolutePath = null;
		Path fileOut = null;
		try{
			fileAbsolutePath = Paths.get(StringUtils.join(uploadDir, File.separatorChar, fullFileName));
			fileOut = Files.write(fileAbsolutePath, filecontent);
		}catch (Exception e) {
			logger.error(AuthConstants.PARAMETER_2, methodName, e.getMessage());
			throw e;
		}
		return fileOut;
	}
	/**
	 * Resize image for profile picture
	 * 
	 * @param inputStream
	 * @param extension
	 * @return scaled image to be used as profile picture
	 * @throws Exception
	 */
	public static byte[] resizeImageAsThumbnails(InputStream inputStream, String extension, int[] widthHeight) throws IOException {
		Assert.notNull(inputStream, "Image data should not be null.");
		Assert.notNull(extension, "Extension can not be null.");
		String methodName = "resizeImageAsThumbnails()";
		byte[] imageInByte = null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedImage img = ImageIO.read(inputStream);
			BufferedImage scaledImg = Scalr.resize(img, Mode.AUTOMATIC, widthHeight[0], widthHeight[1]);
	
			ImageIO.write(scaledImg, extension, baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		}catch(Exception e){
			logger.error(AuthConstants.PARAMETER_2, methodName, e.getMessage());
			throw e;
		}
		return imageInByte;
	}
	
	/**
	 * 
	 * Convert profile-picture/avatar file to base64 string
	 * 
	 * @param String path 
	 * @throws IOException 
	 */
	public static String userAvatarToString(String path) throws IOException {
		if (!StringUtils.isBlank(path)) {
			Path fileOut = Paths.get(path);
			return Base64.getEncoder().encodeToString(Files.readAllBytes(fileOut));
		}
		return StringUtils.EMPTY;
	}
}
