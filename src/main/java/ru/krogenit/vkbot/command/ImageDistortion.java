package ru.krogenit.vkbot.command;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.messages.MessageAttachmentType;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.PhotoSizesType;

import ru.krogenit.vkbot.ImageDistortionQueue;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.user.VkUser;

public class ImageDistortion extends BotCommand {
	
	ImageDistortionQueue imageDist;

	public ImageDistortion() {
		super(new String[] {"сожми", "разъеби"}, true);
		this.imageDist = new ImageDistortionQueue(vk, actor);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		int random = rand.nextInt(5);
		if (random == 0) sender.sendMessage("ща пагади, разъебу", chatId, msgId, userId, false);
		else if (random == 1) sender.sendMessage("5 сек", chatId, msgId, userId, false);
		else if (random == 2) sender.sendMessage("жди", chatId, msgId, userId, false);
		else if (random == 3) sender.sendMessage("ща", chatId, msgId, userId, false);
		else if (random == 4) sender.sendMessage("ок ща", chatId, msgId, userId, false);
		if(StringUtils.containsIgnoreCase(usedCommand, "сожми")) distortImage(ImageDistortionQueue.ImageProcType.Jpeg, msg, chatId, msgId, userId);
		else distortImage(ImageDistortionQueue.ImageProcType.Dist, msg, chatId, msgId, userId);
		return true;
	}
	
	private void distortImage(ImageDistortionQueue.ImageProcType type, Message msg, int chatId, int msgId, int userId) {
		List<MessageAttachment> atts = msg.getAttachments();
		if ((atts == null || atts.size() == 0) || atts.get(0).getType() != MessageAttachmentType.PHOTO) {
			sender.sendMessage("саси, хуле фотку не прикрепил", chatId, msgId, userId, false);
			return;
		}

		MessageAttachment att = atts.get(0);
		Photo photo = att.getPhoto();

		URL url = null;

		try {
			if (photo.getPhoto807() != null) url = new URL(photo.getPhoto807());
			else if (photo.getPhoto604() != null) url = new URL(photo.getPhoto604());
			else {
				boolean found = false;
				List<PhotoSizes> sizes = photo.getSizes();
				
				for(PhotoSizes size : sizes) {
					if(size.getType() == PhotoSizesType.Z) {
						url = new URL(size.getUrl());
						found = true;
						break;
					}
				}
				
				if(!found)
					for(PhotoSizes size : sizes) {
						if(size.getType() == PhotoSizesType.Y) {
							url = new URL(size.getUrl());
							found = true;
							break;
						}
					}
				
				if(!found)
					for(PhotoSizes size : sizes) {
						if(size.getType() == PhotoSizesType.X) {
							url = new URL(size.getUrl());
							found = true;
							break;
						}
					}
				
				if(!found)
					for(PhotoSizes size : sizes) {
						if(size.getType() == PhotoSizesType.M) {
							url = new URL(size.getUrl());
							found = true;
							break;
						}
					}
			}

			Image image1 = new ImageIcon(url).getImage();
			imageDist.addImageToQueue(type, image1, userId, chatId, msgId, Main.isFromGroup);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
