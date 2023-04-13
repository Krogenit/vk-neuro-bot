package ru.krogenit.vkbot.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.ContentMover;
import ru.krogenit.vkbot.user.VkUser;

public class AttachPostCommand extends BotCommand {

	private List<Integer> ownerIds;
	public static HashMap<String, Integer> wallSize = new HashMap<String, Integer>();
	
	public AttachPostCommand(String[] commands, int[] ownerid) {
		super(commands, true);
		this.ownerIds = new ArrayList<Integer>();
		for(int i=0;i<ownerid.length;i++) ownerIds.add(ownerid[i]);
	}
	
	public AttachPostCommand(String[] commands, int ownerid) {
		super(commands, true);
		this.ownerIds = new ArrayList<Integer>();
		this.ownerIds.add(ownerid);
	}
	
	public AttachPostCommand(String commands, int ownerid) {
		super(commands, true);
		this.ownerIds = new ArrayList<Integer>();
		this.ownerIds.add(ownerid);
	}
	
	public AttachPostCommand(String commands, int[] ownerid) {
		super(commands, true);
		this.ownerIds = new ArrayList<Integer>();
		for(int i=0;i<ownerid.length;i++) ownerIds.add(ownerid[i]);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		int ownerId = ownerIds.get(rand.nextInt(ownerIds.size()));
		
		attachRandomPost(ownerId, chatId, msgId, userId);
		
		return true;
	}
	
	protected int getWallSize(int ownerId) {
		String wallKey = ownerId + "";
		Integer size = wallSize.get(wallKey);
		System.out.println("[Wall Post] get buffered wall size wall: " + wallKey + " size: " + size);
		if (size == null) {
			GetResponse resp = null;
			try {
				resp = vk.wall().get(actor).ownerId(ownerId).execute();
			} catch (ApiException | ClientException e) {
				e.printStackTrace();
			}
			size = resp.getCount();
			wallSize.put(wallKey, size);
			System.out.println("[Wall Post] get new wall size wall: " + wallKey + " size: " + size);
		}

		return size;
	}
	
	protected void attachRandomPost(int ownerid, int chatId, int msgId, int userId) {
		int offset1 = rand.nextInt(getWallSize(ownerid));
		System.out.println("[Wall Post] offset: " + offset1);
		
//		int peerId = chatId != 0 ? chatId : userId;
		
//		try {
//			JsonElement element = vk.execute().code(actor, "var post = API.wall.get({\"owner_id\":"+ownerid+",\"count\":1,\"offset\":"+offset1+"});\n" + 
//					"\r\n" + 
//					"var ownerId = post.items[0].owner_id;\n" + 
//					"var postId = post.items[0].id;\n" + 
//					"var wallPost = \"wall\" + ownerId + \"_\" + postId;\n" + 
//					"\n" + 
//					"return API.messages.send({\"random_id\":"+rand.nextInt()+",\"peer_id\":"+peerId+",\"attachment\":wallPost,\"group_id\":"+Main.GROUP_ID+"});\r\n").execute();
//			System.out.println(element);
//			
//		}catch (ApiException | ClientException | InterruptedException e) {
//			e.printStackTrace();
//		}
		
		
		GetResponse resp = null;
		try {
			resp = vk.wall().get(actor).ownerId(ownerid).count(1).offset(offset1).execute();
		} catch (ApiException | ClientException e) {
			e.printStackTrace();
		}
		if(resp != null) attachWallPost(resp.getItems().get(0), chatId, msgId, userId);
	}
	
	protected void attachWallPost(WallPostFull post, int chatId, int msgId, int userId) {
		ContentMover mover = CommandHandler.INSTANCE.mover;
		String newContent = null;
		if(mover.needMoveContent(post.getOwnerId())) {
			newContent = mover.getNewContent("wall" + post.getOwnerId() + "_" + post.getId());
			if(newContent == null) {
				newContent = mover.movePost(post);
			}
			
			if(newContent == null) {
				System.out.println("[Wall Post] ERROR HASHING WALL POST: " + post.getOwnerId() + " postId: " + post.getId());
				return;
			}
			
			System.out.println("[Wall Post] attached wall post owner: " + post.getOwnerId() + " postId: " + post.getId());
			List<String> atts = new ArrayList<String>();
			atts.add(newContent);
			sender.sendMessage("", chatId, msgId, userId, false, atts, null);
		} else {
			List<String> atts = new ArrayList<String>();
			atts.add("wall" + post.getOwnerId() + "_" + post.getId());
			sender.sendMessage("", chatId, msgId, userId, false, atts, null);
		}
	}

}
