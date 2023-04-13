package ru.krogenit.vkbot;

import ij.ImagePlus;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import khl.dip.assignment.Carve;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiCaptchaException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.objects.photos.responses.WallUploadResponse;

import ru.krogenit.vkbot.notify.NotificationHandler;

public class ImageDistortionQueue {

	public static ImageDistortionQueue INSTANCE;
	public static boolean threadRun;

	VkApiClient vk;
	UserActor actor;

	public enum ImageProcType {
		Dist, Jpeg
	}

	public class ImageReciver {
		public String imageName;
		public int userId, chatId, msgId, wallId, replyId;
		public ImageProcType type;
		public boolean isFromGroup;

		public ImageReciver(ImageProcType type, String imageName, int userId, int chatId, int msgId, boolean isFromGroup) {
			this.userId = userId;
			this.chatId = chatId;
			this.msgId = msgId;
			this.imageName = imageName;
			this.type = type;
			this.isFromGroup = isFromGroup;
		}

		public ImageReciver(ImageProcType type, int userId, int wallId, int replyId, String imageName, boolean isFromGroup) {
			this.imageName = imageName;
			this.type = type;
			this.wallId = wallId;
			this.replyId = replyId;
			this.userId = userId;
			this.isFromGroup = isFromGroup;
		}
	}

	public class ImageDistortionThread extends Thread {
		List<ImageReciver> imageToProcess = Collections.synchronizedList(new ArrayList<ImageReciver>());

		public void addImageReciver(ImageReciver rec) {
			this.imageToProcess.add(rec);
		}

		@Override
		public void run() {
			while (threadRun && Main.isRun) {
				try {
					while (imageToProcess.size() > 0) {
						ImageReciver rec = imageToProcess.remove(0);
						System.out.println("[PHOTO DIST] Add new photo");

						switch (rec.type) {
						case Dist:
							new Carve(rec.imageName).run();
							break;
						case Jpeg:
							processImageToJpeg(rec.imageName);
							break;
						}

						System.out.println("[PHOTO DIST] Photo processed");
						processedImages.add(rec);
					}

					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	ImageDistortionThread thread = new ImageDistortionThread();

	List<ImageReciver> processedImages = Collections.synchronizedList(new ArrayList<ImageReciver>());

	int uniqueImageId = 0;

	public ImageDistortionQueue(VkApiClient vk, UserActor actor) {
		this.vk = vk;
		this.actor = actor;
		INSTANCE = this;
	}

	private void processImageToJpeg(String name) {
		ImagePlus image = new ImagePlus(name);

		JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
		jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpegParams.setCompressionQuality(0.0f);

		final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

		try {
			writer.setOutput(new FileImageOutputStream(new File(name.replace(".jpg", "dist.jpg"))));
			writer.write(null, new IIOImage(image.getBufferedImage(), null, null), jpegParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addImageFromWall(ImageProcType type, Image img, int wallId, int replyId, int userId, boolean isFromGroup) {
		String iamgeName = "test" + uniqueImageId + ".jpg";
		File f = new File("images", iamgeName);

		while (f.exists()) {
			uniqueImageId++;
			iamgeName = "test" + uniqueImageId + ".jpg";
			f = new File("images", iamgeName);
		}

		try {
			ImageIO.write((RenderedImage) img, "JPG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!threadRun) {
			threadRun = true;
			thread.start();
		}

		ImageReciver rec = new ImageReciver(type, userId, wallId, replyId, "images/" + iamgeName, isFromGroup);
		thread.addImageReciver(rec);

		uniqueImageId++;
	}

	public void addImageToQueue(ImageProcType type, Image img, int userId, int chatId, int msgId, boolean isFromGroup) {
		String iamgeName = "test" + uniqueImageId + ".jpg";
		File f = new File("images", iamgeName);

		while (f.exists()) {
			uniqueImageId++;
			iamgeName = "test" + uniqueImageId + ".jpg";
			f = new File("images", iamgeName);
		}

		try {
			BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = bi.createGraphics();

			g2.drawImage(img, 0, 0, null);
			g2.dispose();
			ImageIO.write(bi, "JPG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!threadRun) {
			threadRun = true;
			thread.start();
		}

		ImageReciver rec = new ImageReciver(type, "images/" + iamgeName, userId, chatId, msgId, isFromGroup);
		thread.addImageReciver(rec);

		uniqueImageId++;
	}

	private void uploadToMessage(ImageReciver rec) throws Exception {
		if(rec.isFromGroup) {
			PhotoUpload pu = vk.photos().getMessagesUploadServer(Main.INSTANCE.group).execute();
			MessageUploadResponse mur = vk.upload().photoMessage(pu.getUploadUrl(), new File(rec.imageName.replace(".jpg", "dist.jpg"))).execute();
			List<Photo> photos = null;
			try {
				photos = vk.photos().saveMessagesPhoto(Main.INSTANCE.group, mur.getPhoto()).server(mur.getServer()).hash(mur.getHash()).execute();
			} catch (ApiCaptchaException captcha) {
				System.out.println("[CAPTCHA] Error on saving voice msg");
				CommandHandler.INSTANCE.processCapcha(captcha);
	
				try {
					photos = vk.photos().saveMessagesPhoto(Main.INSTANCE.group, mur.getPhoto()).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
					CommandHandler.INSTANCE.capcha = null;
					System.out.println("[CAPTCHA] Success upload photo");
				} catch (ApiCaptchaException captcha1) {
					System.out.println("[CAPTCHA] Upload photo failed");
					return;
				}
			}
			Photo photo = photos.get(0);
			List<String> atts = new ArrayList<String>();
			atts.add("photo" + photo.getOwnerId() + "_" + photo.getId());
			Main.isFromGroup = true;
			CommandHandler.INSTANCE.sender.sendMessage("", rec.chatId, rec.msgId, rec.userId, false, atts, null);
			Main.isFromGroup = false;
		} else {
			PhotoUpload pu = vk.photos().getMessagesUploadServer(actor).execute();
			MessageUploadResponse mur = vk.upload().photoMessage(pu.getUploadUrl(), new File(rec.imageName.replace(".jpg", "dist.jpg"))).execute();
			List<Photo> photos = null;
			try {
				photos = vk.photos().saveMessagesPhoto(actor, mur.getPhoto()).server(mur.getServer()).hash(mur.getHash()).execute();
			} catch (ApiCaptchaException captcha) {
				System.out.println("[CAPTCHA] Error on saving voice msg");
				CommandHandler.INSTANCE.processCapcha(captcha);
	
				try {
					photos = vk.photos().saveMessagesPhoto(actor, mur.getPhoto()).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
					CommandHandler.INSTANCE.capcha = null;
					System.out.println("[CAPTCHA] Success upload photo");
				} catch (ApiCaptchaException captcha1) {
					System.out.println("[CAPTCHA] Upload photo failed");
					return;
				}
			}
			Photo photo = photos.get(0);
			List<String> atts = new ArrayList<String>();
			atts.add("photo" + photo.getOwnerId() + "_" + photo.getId());
			CommandHandler.INSTANCE.sender.sendMessage("", rec.chatId, rec.msgId, rec.userId, false, atts, null);
		}
	}

	private void uploadToWall(ImageReciver rec) throws Exception {
		if(rec.isFromGroup) {
			
		} else {
			PhotoUpload pu = vk.photos().getWallUploadServer(actor).execute();
			WallUploadResponse mur = vk.upload().photoWall(pu.getUploadUrl(), new File(rec.imageName.replace(".jpg", "dist.jpg"))).execute();
			List<Photo> photos = null;
			try {
				photos = vk.photos().saveWallPhoto(actor, mur.getPhoto()).server(mur.getServer()).hash(mur.getHash()).execute();
			} catch (ApiCaptchaException captcha) {
				System.out.println("[CAPTCHA] Error on saving voice msg");
				CommandHandler.INSTANCE.processCapcha(captcha);

				try {
					photos = vk.photos().saveWallPhoto(actor, mur.getPhoto()).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
					CommandHandler.INSTANCE.capcha = null;
					System.out.println("[CAPTCHA] Success upload photo");
				} catch (ApiCaptchaException captcha1) {
					System.out.println("[CAPTCHA] Upload photo failed");
					return;
				}
			}
			Photo photo = photos.get(0);
			List<String> atts = new ArrayList<String>();
			atts.add("photo" + photo.getOwnerId() + "_" + photo.getId());
			if (rec.replyId != 0) NotificationHandler.INSTANCE.replyOnWallCaommentWithAttachments("", rec.wallId, rec.replyId, rec.userId, atts);
			else NotificationHandler.INSTANCE.sendCommentToWallWithAttachments("", rec.wallId, rec.userId, atts);
		}
	}

	public void processQueue() throws Exception {
		while (processedImages.size() > 0) {
			ImageReciver rec = processedImages.remove(0);
			if (rec.wallId != 0) {
				uploadToWall(rec);
			} else {
				uploadToMessage(rec);
			}
		}
	}
}
