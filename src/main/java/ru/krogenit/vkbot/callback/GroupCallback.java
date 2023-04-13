package ru.krogenit.vkbot.callback;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll;
import com.vk.api.sdk.callback.objects.wall.CallbackWallComment;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.audio.Audio;
import com.vk.api.sdk.objects.board.TopicComment;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.wall.PostType;
import com.vk.api.sdk.objects.wall.WallPost;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.queries.wall.WallPostQuery;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.MessageHandler;

public class GroupCallback extends CallbackApiLongPoll {

	VkApiClient vk;
	UserActor user;
	GroupActor group;
	
	public GroupCallback(VkApiClient client, GroupActor group, UserActor user) {
		super(client, group, 25);
		this.vk = client;
		this.user = user;
		this.group = group;
	}
	
	@Override
	public void messageNew(Integer groupId, Message message) {
//		Main.isRun = false;
		MessageHandler.INSTANCE.processMessage(message, true);
		
		
//		System.out.println("[Group] new message " + message.getBody());
//		Integer id = message.getUserId();
//		String text = message.getBody();
//		
//		if(text == null || text.length() == 0) {
//			text = "высер";
//		}
//		
//		try {
//			vk.messages().send(group).userId(id).randomId(CommandHandler.INSTANCE.rand.nextInt()).message(CommandHandler.INSTANCE.neuro.newMessage(text)).execute();
//			
//		} catch (ApiException | InterruptedException | ClientException | IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void photoNew(Integer groupId, Photo photo) {
		System.out.println("[Group] new photo ");
		
//		try {
//			vk.photos().createComment(user, photo.getId()).ownerId(photo.getOwnerId()).fromGroup(true).accessKey(photo.getAccessKey()).message(CommandHandler.INSTANCE.neuro.newMessage("фото")).execute();
//		} catch (ApiException | ClientException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void videoNew(Integer groupId, Video video) {
		System.out.println("[Group] new video ");
		
//		try {
//			vk.videos().createComment(user, video.getId()).ownerId(video.getOwnerId()).message(CommandHandler.INSTANCE.neuro.newMessage("видео")).execute();
//			
//		} catch (ApiException | ClientException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void wallPostNew(Integer groupId, WallPost post) {
//		System.out.println("[Group] new post ");
		
		PostType postType = post.getPostType();
		switch(postType) {
		case SUGGEST:
			boolean anon = post.getText() != null && (StringUtils.startsWithIgnoreCase(post.getText(), "анон") || StringUtils.endsWithIgnoreCase(post.getText(), "анон"));
			
			WallPostQuery query = vk.wall().post(user).ownerId(post.getOwnerId()).fromGroup(true).signed(!anon).postId(post.getId());
			
			if(post.getText() != null && post.getText().length() > 0) {
				query = query.message(post.getText());
			}
			
			List<String> atts = new ArrayList<String>();
			List<WallpostAttachment> atts1 = post.getAttachments();
			if(atts1 != null && atts1.size() > 0) {
				for(WallpostAttachment att : atts1) {
					atts.add(getAttByType(att));
				}
				
				query = query.attachments(atts);
			}
			
			try {
				query.execute();
				
			} catch (ApiException | ClientException e) {
				e.printStackTrace();
//				System.out.println("PARAMS: " + );
			}
			
			break;
		}
	}
	
	@Override
	public void wallReplyNew(Integer groupId, CallbackWallComment comment) {
		if(comment.getFromId() == -Main.GROUP_ID) return;
		System.out.println("[Group] new comment " + comment.getText());
		
		String text = comment.getText();
		if(text == null || text.length() == 0) {
			text = "высер";
		}
		
		if(CommandHandler.INSTANCE.rand.nextInt(3) != 0) return;
		
		try {
			vk.wall().createComment(user, comment.getPostId()).ownerId(-groupId).message(CommandHandler.INSTANCE.neuro.newMessage(text)).replyToComment(comment.getId()).
			fromGroup(groupId).execute();	
		} catch (ApiException | ClientException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void boardPostNew(Integer groupId, TopicComment comment) {
//		comment.get
//		vk.board().createComment(user, groupId, topicId)
	}
	
	public String getAttByType(WallpostAttachment att) {
		WallpostAttachmentType type = att.getType();
		switch(type) {
		case PHOTO:
			Photo photo = att.getPhoto();
			
			return "photo" + photo.getOwnerId() + "_" + photo.getId();
		case VIDEO:
			Video video = att.getVideo();
			
			return "video" + video.getOwnerId() + "_" + video.getId();
		case AUDIO:
			Audio audio = att.getAudio();

			return "audio" + audio.getOwnerId() + "_" + audio.getId();
		case DOC:
			Doc doc = att.getDoc();
			
			return "doc" + doc.getOwnerId() + "_" + doc.getId();
		}
		
		return "";
	}
}
