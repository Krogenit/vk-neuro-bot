package ru.krogenit.vkbot.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.responses.GetResponse;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.ContentMover;
import ru.krogenit.vkbot.chat.VkChat;
import ru.krogenit.vkbot.user.VkUser;

public class AttachPhotoFromAlbum extends BotCommand {
	
	static HashMap<String, Integer> albumSize = new HashMap<String, Integer>();

	ArrayList<String> ownerAndAlbumIds = new ArrayList<String>();
	boolean onlyForLocalChat;
	
	public AttachPhotoFromAlbum(String commandNames, String[] ownerAndAlbumIds) {
		super(commandNames, true);
		for(String s : ownerAndAlbumIds) this.ownerAndAlbumIds.add(s);
	}

	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		if(onlyForLocalChat) {
			if(chatId == VkChat.LOCAL_CHAT) {
				attachRandomPhoto(chatId, userId);
				return true;
			}
		} else {
			attachRandomPhoto(chatId, userId);
			return true;
		}
		
		return false;
	}
	
	public AttachPhotoFromAlbum setOnlyForLocalChat() {
		this.onlyForLocalChat = true;
		return this;
	}
	
	private void attachRandomPhoto(int chatId, int userId) {
		int size = ownerAndAlbumIds.size();
		int id = rand.nextInt(size);
		String[] ownerAndAlbum = ownerAndAlbumIds.get(id).split("_");
		String sowner = ownerAndAlbum[0];
		String album = ownerAndAlbum[1];
		Integer ownerId = Integer.parseInt(sowner);
		
		try {
			GetResponse photos = vk.photos().get(actor).ownerId(ownerId).albumId(album).offset(rand.nextInt(getAlbumSizze(ownerId, album))).count(1).execute();
			Photo photo = photos.getItems().get(0);
			ContentMover mover = CommandHandler.INSTANCE.mover;
			String newContent = null;
			if(mover.needMoveContent(photo.getOwnerId())) {
				newContent = mover.getNewContent("photo" + photo.getOwnerId() + "_" + photo.getId());
				if(newContent == null) {
					newContent = mover.movePhoto(photo);
				}
				
				if(newContent == null) {
					System.out.println("[Random photo] ERROR HASHING PHOTO: " + photo.getOwnerId() + " postId: " + photo.getId());
					return;
				}

				List<String> atts = new ArrayList<String>();
				atts.add(newContent);
				sender.sendMessage("", chatId, 0, userId, false, atts, null);
			} else {
				List<String> atts = new ArrayList<String>();
				atts.add("photo" + photo.getOwnerId() + "_" + photo.getId());
				sender.sendMessage("", chatId, 0, userId, false, atts, null);
			}
		} catch (ApiException e) {
			e.printStackTrace();
			sender.sendMessage("блять не удалось фотку прикрепить " + e.getMessage(), chatId, 0, userId, false);
		} catch (ClientException e) {
			e.printStackTrace();
			sender.sendMessage("блять не удалось фотку прикрепить " + e.getMessage(), chatId, 0, userId, false);
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage("блять не удалось фотку прикрепить " + e.getMessage(), chatId, 0, userId, false);
		}
	}
	
	private int getAlbumSizze(int ownerId, String album) throws Exception {
		String albumKey = ownerId + "_" + album;
		Integer size = albumSize.get(albumKey);

		if (size == null) {
			GetResponse resp = vk.photos().get(actor).ownerId(ownerId).albumId(album).execute();
			size = resp.getCount();
			albumSize.put(albumKey, size);
			
		}

		return size;
	}
}