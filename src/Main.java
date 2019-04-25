import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.opencsv.CSVWriter;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Comment;
import com.restfb.types.Group;
import com.restfb.types.Likes.LikeItem;
import com.restfb.types.Post;
import com.restfb.types.Reactions.ReactionItem;

/**
 * TODO: 
 * Facebook authentication (no more temporary access codes)
 * 
 *
 */

public class Main {
	/** 
	 * @param args the command line arguments
	 */
	public static void main(String accessToken) {
		FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_3_1);
		
		Connection<Group> userGroups = fbClient.fetchConnection("me/groups", Group.class);
		//get the iterator
		Iterator<List<Group>> groupIt = userGroups.iterator();
		while(groupIt.hasNext()) {
			List<Group> page = groupIt.next();
			for(Group currGroup : page) {
				try { 
					Connection<Post> postFeed = fbClient.fetchConnection(currGroup.getId()+"/feed", Post.class, Parameter.with("fields","from,actions,message,likes,reactions,story,type,link,picture,created_time,comments"));
					writeCSVData(currGroup, postFeed, fbClient);
					}
					catch(FacebookOAuthException e) {
						// Don't throw an error if the user isn't an admin - just skip
					}

			}
		}
	}
	public static void main(String[] args) {
		String AppID = "326587264865204";
		//String accessToken = "EAAEpB4XpE7QBAJuetd9veebUHZC7DOieyO4jGnnBQRtr4swo6IFNUwZBUkKUvHnzvAoEouEtj936n6sasTsNgtX3tuoSXE6UkdxNFN1sZB2KPP0vJtE7DLvwyhhQqsLqZAZCzorVlnVocZCD7ZALAMsWzZAD13FcLvZBG5yCEynePum92yIT9KIBYpEzbdiOb9shSkeZC73yijwgZDZD";
		
		// Token expires daily - facebook auth needed for permanent one or app needs to login with facebook
		// Just MY access token for now - should be replaced with an admin
		
		
		Scanner input = new Scanner(System.in);
		System.out.println("Enter user access token: ");
		String accessToken = input.nextLine();
		FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_3_1);
		
		Connection<Group> userGroups = fbClient.fetchConnection("me/groups", Group.class);
		//get the iterator
		Iterator<List<Group>> groupIt = userGroups.iterator();
		while(groupIt.hasNext()) {
			List<Group> page = groupIt.next();
			for(Group currGroup : page) {
				// Will need to be changed in final version
				if(currGroup.getName() == "Tests") { // In final version just get all group info
					Connection<Post> postFeed = fbClient.fetchConnection(currGroup.getId()+"/feed", Post.class, Parameter.with("fields","from,actions,message,likes,reactions,story,type,link,picture,created_time,comments"));
					writeCSVData(currGroup, postFeed, fbClient);
					System.out.println("Done!");
				}
			}
		}
		input.close();
		
				
	}
	public static void writeCSVData(Group currGroup, Connection<Post> postFeed, FacebookClient fbClient) 
	{ 


		Iterator<List<Post>> it = postFeed.iterator();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM.dd.yyyy hh.mm.ss");
		LocalDateTime now = LocalDateTime.now();
        DateFormat csvDateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
	    try {
	    	// Should first create a folder then put all of these files in that folder with the date
	    	// Create the chat_feed csv file
	    	File file = new File("chat_feed "+currGroup.getName()+"_"+dtf.format(now)+".csv"); // Filepath needs to be more specific in final
	    	FileWriter outputfile = new FileWriter(file);
		    // create CSVWriter object filewriter object as parameter 
			CSVWriter chatWriter = new CSVWriter(outputfile);
			List<String[]> chat_data = new ArrayList<String[]>();
			chat_data.add(new String[] {"Id", "UserId", "UserName", "CreatedTime", "StatusType", "Message", "Story", "Link", "Picture"});

			// create the likes csv file
			File likeFile = new File("likes_feed "+currGroup.getName()+"_"+dtf.format(now)+".csv");      // Filepath needs to be more specific in final
	    	FileWriter likeOutputfile = new FileWriter(likeFile);
			CSVWriter likeWriter = new CSVWriter(likeOutputfile);
			List<String[]> like_data = new ArrayList<String[]>();
			like_data.add(new String[] {"ObjectId", "UserId", "UserName"});
			
			// create the likes csv file
			File reactionFile = new File("reactions_feed "+currGroup.getName()+"_"+dtf.format(now)+".csv");      // Filepath needs to be more specific in final
			FileWriter reactionOutputfile = new FileWriter(reactionFile);
			CSVWriter reactionWriter = new CSVWriter(reactionOutputfile);
			List<String[]> reaction_data = new ArrayList<String[]>();
			reaction_data.add(new String[] {"ObjectId", "UserId", "UserName", "Type"});

			
			// create the comments csv file
			File commentFile = new File("comments_feed "+currGroup.getName()+"_"+dtf.format(now)+".csv"); // Filepath needs to be more specific in final
	    	FileWriter commentOutputFile = new FileWriter(commentFile);
			CSVWriter commentWriter = new CSVWriter(commentOutputFile);
			List<String[]> comment_data = new ArrayList<String[]>();
			comment_data.add(new String[] {"PostId", "Id", "UserId", "UserName", "CreatedTime", "Message"});			
			
	    	while(it.hasNext()) {
	    		List<Post> feedPage = it.next();
				//Create a list of posts from the post feed Y
				for(Post currPost : feedPage) {
					String id = currPost.getId();
					String UserId;
					String UserName;
					// Need permissions to get username
					try {
						UserId =  currPost.getFrom().getId();
						UserName = currPost.getFrom().getName();
					} catch(NullPointerException e) {
						UserId = "Unauthorized User";
						UserName = "Unauthorized User";
					}
					String CreatedTime = csvDateFormat.format(currPost.getCreatedTime());
					String type = currPost.getType();
					String message = currPost.getMessage();
					String story = currPost.getStory();
					String link = "fb.com/"+id;
					String pic = currPost.getPicture();
					
					//IF post isnt shared, add to chat_data file
					chat_data.add(new String[] {id,UserId,UserName,CreatedTime,type,message,story,link,pic});
					//else add to shares file
					if(currPost.getLikes() != null) {
						// This only recognizes when the current authorized user token likes a post
						//add to the likes.csv file
						for (LikeItem currLike : currPost.getLikes().getData()) {
							String LikeUserId;
							String LikeUserName;
							try {
								LikeUserId = currLike.getId();
								LikeUserName = currLike.getName();
							} catch(NullPointerException e) {
								LikeUserId = "UserId Unavailable";
								LikeUserName = "UserName Unavailable";
							}
							
							
							like_data.add(new String[] {id, LikeUserId, LikeUserName});
						}
						

					}
					
					if (currPost.getReactions() != null) {
						// This only recognizes when the current authorized user reacts
						for (ReactionItem currReact : currPost.getReactions().getData()) {
							String rType = currReact.getType();
							String rUserId;
							String rUserName;
							try {
								rUserId = currReact.getId();
								rUserName = currReact.getName();
							} catch(NullPointerException e) {
								rUserId = "UserId Unavailable";
								rUserName = "UserName Unavailable";
							}
							
							reaction_data.add(new String[] {id, rUserId, rUserName, rType});
						}
					}
					
					if (currPost.getComments() != null) {
						//add to the comments.csv file
						for (Comment currComment : currPost.getComments().getData()) {
					            // postid is id
					            String commentID = currComment.getId();
								// Can't use getFrom with out user permissions
								String commentUserId;
								String commentUserName;
								try {
									commentUserId = currComment.getFrom().getId();
									commentUserName = currComment.getFrom().getName();
								} catch(NullPointerException e) {
									commentUserId = "UserId Unavailable";
									commentUserName = "UserName Unavailable";
								}
								String commentCreatedTime = csvDateFormat.format(currComment.getCreatedTime());
								String commentMessage = currComment.getMessage();
								comment_data.add(new String[] {id,commentID,commentUserId,commentUserName,commentCreatedTime,commentMessage});
								// check if the comment has any likes or reacts
								
					    }
					}
				}
			}
	    	chatWriter.writeAll(chat_data);
	    	likeWriter.writeAll(like_data);
	    	commentWriter.writeAll(comment_data);
	    	reactionWriter.writeAll(reaction_data);
	    	chatWriter.close();
	    	likeWriter.close();
	    	commentWriter.close();
	    	reactionWriter.close();
		       
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

}
