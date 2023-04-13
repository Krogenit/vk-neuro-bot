package ru.krogenit.vkbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.chat.ChatHandler;

public class MessageSaver {
	HashMap<Integer, FileWriter> wr;
	HashMap<Integer, StringBuilder> chatMessages;

	public MessageSaver() {
		wr = new HashMap<Integer, FileWriter>();
		chatMessages = new HashMap<Integer, StringBuilder>();
	}

	public void addConv(int chatId, String name) {
		if (Main.isDebug) return;
		try {
			this.wr.put(chatId, (new FileWriter(new File("messages", name + ".txt"), true)));
			this.chatMessages.put(chatId, new StringBuilder());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() throws IOException {
		if (Main.isDebug) return;
		Iterator<Integer> keys = wr.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = keys.next();
			FileWriter wr = this.wr.get(key);
			StringBuilder messages = this.chatMessages.get(key);

			wr.write(messages.toString());
			wr.flush();
			wr.close();
		}

		wr.clear();
		chatMessages.clear();
	}

	public void stop() throws IOException {
		if (Main.isDebug) return;
		Iterator<Integer> keys = wr.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = keys.next();
			FileWriter wr = this.wr.get(key);

			wr.close();
		}
		
		wr.clear();
	}

	public void writeMsg(Message msg, String firstName, String lastName, int chatId) {
		if (Main.isDebug) return;
		if (msg.getText() != null && msg.getText().length() > 0) {
			if (chatId != 0 && this.wr.containsKey(chatId) && ChatHandler.INSTACNE.getChat(chatId) != null && !ChatHandler.INSTACNE.getChat(chatId).canTalk) {
				StringBuilder messages = this.chatMessages.get(chatId);

				String mes = msg.getText();

				if (StringUtils.startsWithIgnoreCase(mes, "ыля")) return;

				while (mes.contains("\n")) {
					int index = mes.indexOf("\n");
					if (mes.charAt(index - 1) == ',' || mes.charAt(index - 1) == '.' || mes.charAt(index - 1) == '!' || mes.charAt(index - 1) == '?') {
						mes = mes.replaceFirst("\n", " ");
					} else {
						mes = mes.replaceFirst("\n", ", ");
					}
				}

				String res = firstName + " " + lastName + ":" + mes + "\n";
				messages.append(res);
			} else {
				addConv(chatId, "messages" + chatId);
			}
		}
	}
}
