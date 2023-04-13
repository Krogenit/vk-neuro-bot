package ru.krogenit.vkbot;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.PhotoSizesType;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.responses.PostResponse;

public class ContentMover {

	VkApiClient vk;
	GroupActor group;
	UserActor actor;
	
	HashMap<String, String> remapContent = new HashMap<String, String>();
	HashMap<Integer, Integer> remapAlbums = new HashMap<Integer, Integer>();
	List<Integer> privateContent = new ArrayList<Integer>();
	
	Integer savePublicId = -179703374;
	
	public ContentMover() {
		vk = Main.INSTANCE.vk;
		actor = Main.INSTANCE.actor;
		group = Main.INSTANCE.group;
		loadMap();
		loadAlbums();
		loadPrivateContent();
	}
	
	private void loadPrivateContent() {
		privateContent.add(66311705);
		privateContent.add(266779977);
		privateContent.add(261894423);
		privateContent.add(-121242074);
		privateContent.add(-112217202);
	}
	
	public String movePost(WallPostFull post) {
		String postString = "wall" + post.getOwnerId() + "_" + post.getId();
		System.out.println("[PHOTO MOVER] Started move post " + postString);
		
		List<String> atts = new ArrayList<String>();
		if(post.getAttachments() != null) for(int i=0;i<post.getAttachments().size();i++) {
			WallpostAttachment att = post.getAttachments().get(i);
			if(att.getType() == WallpostAttachmentType.AUDIO) {
				atts.add("audio" + att.getAudio().getOwnerId() + "_" + att.getAudio().getId() + "_" + att.getAudio().getAccessKey());
			} else if(att.getType() == WallpostAttachmentType.DOC) {
				atts.add("doc" + att.getDoc().getOwnerId() + "_" + att.getDoc().getId() + "_" + att.getDoc().getAccessKey());
			}else if(att.getType() == WallpostAttachmentType.PHOTO) {
				atts.add("photo" + att.getPhoto().getOwnerId() + "_" + att.getPhoto().getId() + "_" + att.getPhoto().getAccessKey());
			}else if(att.getType() == WallpostAttachmentType.VIDEO) {
				atts.add("video" + att.getVideo().getOwnerId() + "_" + att.getVideo().getId() + "_" + att.getVideo().getAccessKey());
			}
		}
		
		try {
			PostResponse resp = vk.wall().post(actor).ownerId(savePublicId).fromGroup(true).message(post.getText()).attachments(atts).execute();
			String newContent = "wall" + savePublicId + "_" + resp.getPostId();
			remapContent.put(postString, newContent);
			saveMap();
			return newContent;
		} catch (ApiException | ClientException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String movePhoto(Photo photo) {
		System.out.println("[PHOTO MOVER] Started move photo " + photo.getOwnerId() + " " + photo.getId());
		String url = "";
		
		if(photo.getPhoto2560() != null) url = photo.getPhoto2560();
		else if(photo.getPhoto1280() != null) url = photo.getPhoto1280();
		else if(photo.getPhoto807() != null) url = photo.getPhoto807();
		else if(photo.getPhoto604() != null) url = photo.getPhoto604();
		else if(photo.getPhoto130() != null) url = photo.getPhoto130();
		else if(photo.getPhoto75() != null) url = photo.getPhoto75();
		
		if(url.length() == 0) {
			List<PhotoSizes> sizes = photo.getSizes();
			boolean found = false;
			if(sizes != null && sizes.size() > 0) {
				for(PhotoSizes size : sizes) {
					if(size.getType() == PhotoSizesType.W) {
						url = size.getUrl();
						found = true;
						break;
					}
				}
				
				if(!found)
				for(PhotoSizes size : sizes) {
					if(size.getType() == PhotoSizesType.Z) {
						url = size.getUrl();
						found = true;
						break;
					}
				}
				
				if(!found)
					for(PhotoSizes size : sizes) {
						if(size.getType() == PhotoSizesType.Y) {
							url = size.getUrl();
							found = true;
							break;
						}
					}
				
				if(!found)
					for(PhotoSizes size : sizes) {
						if(size.getType() == PhotoSizesType.X) {
							url = size.getUrl();
							found = true;
							break;
						}
					}
				
				if(!found)
					for(PhotoSizes size : sizes) {
						if(size.getType() == PhotoSizesType.M) {
							url = size.getUrl();
							found = true;
							break;
						}
					}
				for(PhotoSizes size : sizes) {
					if(size.getType() == PhotoSizesType.S) {
						url = size.getUrl();
						found = true;
						break;
					}
				}

			} else {
				System.out.println("[PHOTO MOVER] Urls not found ");
			}
		}
		
		if(url.length() > 0) {
			try {
				Image img = new ImageIcon(new URL(url)).getImage();
				File f = new File("upload", "photo" + photo.getOwnerId() + "_" + photo.getId() + ".jpg");
				BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_INDEXED);
				Graphics2D g2 = bi.createGraphics();

				g2.drawImage(img, 0, 0, null);
				g2.dispose();
				
				try {
					ImageIO.write(bi, "JPG", f);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try {
					Integer uploadAlbumId = remapAlbums.get(photo.getOwnerId());
					
					if(uploadAlbumId != null) {
						System.out.println("[PHOTO MOVER] Album found with id " + uploadAlbumId);
					} else {
						System.out.println("[PHOTO MOVER] Album not found for owner " + photo.getOwnerId() + ", creating new...");
						PhotoAlbumFull newAlbum = vk.photos().createAlbum(actor, photo.getOwnerId() + "").commentsDisabled(true).groupId(-savePublicId).uploadByAdminsOnly(true).execute();
						
						uploadAlbumId = newAlbum.getId();
						System.out.println("[PHOTO MOVER] Album created " + uploadAlbumId);
						remapAlbums.put(photo.getOwnerId(), uploadAlbumId);
						saveAlbums();
					}
					
					PhotoUpload resp = vk.photos().getUploadServer(actor).albumId(uploadAlbumId).groupId(-savePublicId).execute();
					PhotoUploadResponse resp1 = vk.upload().photo(resp.getUploadUrl(), f).execute();
					List<Photo> photos = vk.photos().save(actor).albumId(uploadAlbumId).groupId(-savePublicId).hash(resp1.getHash()).server(resp1.getServer()).photosList(resp1.getPhotosList()).execute();
					Photo p = photos.get(0);
					String newContent = "photo" + p.getOwnerId() + "_" + p.getId();
					
					remapContent.put("photo" + photo.getOwnerId() + "_" + photo.getId(), newContent);
					saveMap();			
					return newContent;
				} catch (ApiException | ClientException e) {
					e.printStackTrace();
				}
			} catch (MalformedURLException e2) {
				e2.printStackTrace();
			}
		}
		
		return null;
	}
	
	public String getNewContent(String key) {
		return remapContent.get(key);
	}
	
	public boolean needMoveContent(Integer owner) {
		return Main.isFromGroup && privateContent.contains(owner);
	}
	
	private void loadAlbums() {
		try {

			BufferedReader r = new BufferedReader(new FileReader(new File("upload", "albums.txt")));

			int size = Integer.parseInt(r.readLine());

			for (int i = 0; i < size; i++) {
				String[] map = r.readLine().split("=");
				remapAlbums.put(Integer.parseInt(map[0]), Integer.parseInt(map[1]));
			}
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveAlbums() {
		try {
			FileWriter f = new FileWriter(new File("upload", "albums.txt"));

			Iterator<Integer> iter = remapAlbums.keySet().iterator();
			f.write(remapAlbums.size() + "\n");
			while (iter.hasNext()) {
				Integer key = iter.next();
				Integer r = remapAlbums.get(key);
				f.write(key + "=" + r + "\n");
			}

			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadMap() {
		try {

			BufferedReader r = new BufferedReader(new FileReader(new File("upload", "remap.txt")));

			int size = Integer.parseInt(r.readLine());

			for (int i = 0; i < size; i++) {
				String[] map = r.readLine().split("=");
				remapContent.put(map[0], map[1]);
			}
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveMap() {
		try {
			FileWriter f = new FileWriter(new File("upload", "remap.txt"));

			Iterator<String> iter = remapContent.keySet().iterator();
			f.write(remapContent.size() + "\n");
			while (iter.hasNext()) {
				String key = iter.next();
				String r = remapContent.get(key);
				f.write(key + "=" + r + "\n");
			}

			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
