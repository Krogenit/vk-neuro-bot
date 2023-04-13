package ru.krogenit.vkbot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.audio.Audio;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.messages.Action;
import com.vk.api.sdk.objects.messages.ConversationWithLastMessage;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.messages.MessageAttachmentType;
import com.vk.api.sdk.objects.messages.MessageOld;
import com.vk.api.sdk.objects.messages.responses.GetConversationsResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.video.Video;

import ru.krogenit.vkbot.chat.ChatHandler;
import ru.krogenit.vkbot.user.BlackList;
import ru.krogenit.vkbot.user.UserHandler;
import ru.krogenit.vkbot.user.VkUser;

public class MessageHandler  { 
	
	public static MessageHandler INSTANCE;
	
	public VkApiClient vk;
	public UserActor actor;
	public CommandHandler cmd;
	UserHandler userHandler;
	ChatHandler chatHandler;
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public MessageHandler(VkApiClient vk, UserActor actor, GroupActor group) {
		this.vk = vk;
		this.actor = actor;
		INSTANCE = this;
		this.cmd = new CommandHandler(vk, actor, group);
		this.userHandler = new UserHandler(vk, actor);
		this.chatHandler = new ChatHandler(vk, actor);
	}

	public void update() throws Exception {
		chatHandler.update();
	}
	
	public void processMessage(Message msg, boolean withTime) {
		//if(msg.isOut()) return;
		
		int userId = msg.getUserId();
		int chatId = msg.getChatId();
		int msgId = msg.getId();
		int date = msg.getDate();
		
		if(userId == Main.BOT_ID) return;
		if(chatId == 2 && !Main.BOT_BANNED) return;
		Action action = msg.getAction();
		String message = "";
		if(msg.getText() != null)
			message = msg.getText();
		int time = (int)(System.currentTimeMillis()/1000);
		if(userId < 0) return;
		VkUser user = userHandler.getUser(userId);
		if(user == null) return;
		LocalDateTime date1 = LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.ofHours(+3));
		
		System.out.print((date1 != null ? date1.format(formatter).substring(11) : "")+ "[GROUP]" + "[" + chatId +  "]" + "["+userId+"] " + user.firstName + " " + user.lastName + ": " + message);
		
		List<MessageAttachment> atts = msg.getAttachments();
		if (atts != null && atts.size() > 0) {
			System.out.print(" [Attachments: ");
			for (MessageAttachment att : atts) {
				if (att.getType() == MessageAttachmentType.PHOTO) {
					Photo photo = att.getPhoto();
					System.out.print("photo ");
				} else if (att.getType() == MessageAttachmentType.VIDEO) {
					Video video = att.getVideo();
					System.out.print("video ");
				} else if (att.getType() == MessageAttachmentType.DOC) {
					Doc doc = att.getDoc();
					System.out.print("doc ");
				} else if (att.getType() == MessageAttachmentType.AUDIO) {
					Audio audio = att.getAudio();
					System.out.print("audio"+audio.getOwnerId() + "_" + audio.getId());
				}
			}
			System.out.print("]");
		}
		
		List<Message> fwrdMessages = msg.getFwdMessages();

		if (fwrdMessages != null && fwrdMessages.size() > 0) {
			System.out.print(" [Fwd msgs: ");
			for (Message msg1 : fwrdMessages) {
				String message1 = "";
				if (msg1.getText() != null) message1 = msg1.getText();

				System.out.print("(" + message1 + "), ");
			}
			System.out.print("]");
		}
		
		System.out.println(" ");
		if(BlackList.blockedUsers.contains(Integer.valueOf(userId))) return;
		if(isContainBanWords(message)) return;
		if(withTime && time - date > 60 * 5) return;

		String actionStr = "";
		if(action != null && action.getType() != null) actionStr = action.getType().getValue();
		cmd.processMsg(msg, message, chatId, msgId, userId, user, actionStr, date);
	}
	
	public void processMessage(MessageOld msg, boolean withTime) {
		if(msg.getOut() == 1) return;
		
		int userId = msg.getUserId();
		int chatId = msg.getChatId();
		int msgId = msg.getId();
		int date = msg.getDate();
		if(userId == Main.BOT_ID) return;
//		if(chatId == VkChat.LOCAL_CHAT) return;
		
		String action = msg.getAction();
		String message = "";
		if(msg.getText() != null)
			message = msg.getText();
		int time = (int)(System.currentTimeMillis()/1000);
		if(userId < 0) return;
		VkUser user = userHandler.getUser(userId);
		if(user == null) return;
		LocalDateTime date1 = LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.ofHours(+3));
		
		System.out.print((date1 != null ? date1.format(formatter).substring(11) : "")+ "[STR]" + "[" + chatId +  "]" + "["+userId+"]"  + user.firstName + " " + user.lastName + ": " + message);
		
		List<MessageAttachment> atts = msg.getAttachments();
		if (atts != null && atts.size() > 0) {
			System.out.print(" [Attachments: ");
			for (MessageAttachment att : atts) {
				if (att.getType() == MessageAttachmentType.PHOTO) {
					Photo photo = att.getPhoto();
					System.out.print("photo ");
				} else if (att.getType() == MessageAttachmentType.VIDEO) {
					Video video = att.getVideo();
					System.out.print("video ");
				} else if (att.getType() == MessageAttachmentType.DOC) {
					Doc doc = att.getDoc();
					System.out.print("doc ");
				} else if (att.getType() == MessageAttachmentType.AUDIO) {
					Audio audio = att.getAudio();
					System.out.print("audio"+audio.getOwnerId() + "_" + audio.getId());
				}
			}
			System.out.print("]");
		}
		
		List<MessageOld> fwrdMessages = msg.getFwdMessages();

		if (fwrdMessages != null && fwrdMessages.size() > 0) {
			System.out.print(" [Fwd msgs: ");
			for (MessageOld msg1 : fwrdMessages) {
				String message1 = "";
				if (msg1.getText() != null) message1 = msg1.getText();

				System.out.print("(" + message1 + "), ");
			}
			System.out.print("]");
		}
		
		System.out.println("");
		if(BlackList.blockedUsers.contains(Integer.valueOf(userId))) return;
		if(isContainBanWords(message)) return;
		if(withTime && time - date > 60 * 5) return;

		
		Message newMsg = new Message(msg.getId(), msg.getDate(), msg.getPeerId(), msg.getFromId(), msg.getText(), msg.getBody(), msg.getOut(), msg.getIn(), 
				msg.getReadState(), msg.getConversationMessageId(), msg.getRandomId(), msg.getAttachments(), msg.getImportant(), 
				null, msg.getPayload(), null, false, msg.getActionMid(), msg.getActionEmail(), msg.getActionText());
		cmd.processMsg(newMsg, message, chatId, msgId, userId, user, action, date);
	}
	
	private boolean isContainBanWords(String message) {
		return (StringUtils.containsIgnoreCase(message, "(убери пробелы)") || StringUtils.containsIgnoreCase(message, "лучший сайт для накрутки")
				 || StringUtils.containsIgnoreCase(message, "vto.pe") || StringUtils.containsIgnoreCase(message, "vkbot.ru")
				 ||  StringUtils.containsIgnoreCase(message, "vkbot") || StringUtils.containsIgnoreCase(message, "vto")
				 ||  StringUtils.containsIgnoreCase(message, "сова никогда не спит") ||  StringUtils.containsIgnoreCase(message, "сованикогданеспит")
				 || StringUtils.startsWithIgnoreCase(message, "([club")
				
				
				);
	}
	
	public void checkLs() {
		try {
			int dialogsOffset = 0;
			
			while(true) {
				System.out.println("Get unread conversations with " + dialogsOffset + " offset");
				GetConversationsResponse response = vk.messages().getConversations(actor).filter("unread").offset(dialogsOffset).count(200).execute();
				
				List<ConversationWithLastMessage> dialogs = response.getItems();
				if(dialogs.size() == 0) break;
				for(ConversationWithLastMessage d : dialogs) {
					Message msg = d.getLastMessage();
					if(msg.getUserId() != 0 && msg.getChatId() == 0) {
						MessageHandler.INSTANCE.processMessage(msg, false);
//						GetHistoryResponse resp1 = vk.messages().getHistory(actor).peerId(msg.getUserId()).rev(true).count(200).execute();
						
//						List<Message> msgs = resp1.getItems();
						//List<Integer> msgIds = new ArrayList<Integer>();
//						Integer startMessageId = null;
//						for(int i=msgs.size()-1;i>=0;i--) {
//							Message msg1 = msgs.get(i);
//							if(msg1.getReadState() != null && msg1.getReadState() == 0) {
//								MessageHandler.INSTANCE.processMessage(msg1, false);
//								if(startMessageId == null)
//									startMessageId = msg1.getId();
//								//msgIds.add(msg1.getId());
//							}
//						}
						
//						GetByConversationMessageIdResponse resp2 = vk.messages().getByConversationMessageId(group).peerId(msg.getUserId()).conversationMessageIds(msgIds).execute();
//						List<Message> newMsgs = resp2.getItems();
//						for(int i=newMsgs.size()-1;i>=0;i--) {
//							Message msg1 = newMsgs.get(i);
//							callback.messageNew(-GROUP_ID, msg1);
//						}
//						if(startMessageId != null ) {
//							vk.messages().markAsRead(actor).startMessageId(startMessageId).execute();						
//						}
						vk.messages().markAsRead(actor).peerId(msg.getUserId() + "").execute();
					}
				}
				dialogsOffset += 200;
			}
		} catch (ApiException | ClientException e1) {
			e1.printStackTrace();
		}
	}
	  
	  
} 