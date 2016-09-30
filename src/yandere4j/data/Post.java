package yandere4j.data;

import java.util.Date;

public class Post{

	private File file;
	private Preview preview;
	private Sample sample;

	private long id;
	private long parent_id;
	private long change;

	private String[] tags;

	private String creator_id;
	private String approver_id;
	private String author;
	private String source;
	private String md5;
	private String rating;
	private String status;

	private Date created_at;
	private Date updated_at;

	private boolean is_shown_in_index;
	private boolean is_rating_locked;
	private boolean has_children;
	private boolean is_pending;
	private boolean is_held;
	private boolean is_note_locked;

	private int score;
	private int last_noted_at;
	private int last_commented_at;

	public Post(File file, Preview preview, Sample sample,
			long id, long parent_id, long change,
			String[] tags,
			String creator_id, String approver_id, String author, String source, String md5, String rating, String status,
			Date created_at, Date updated_at,
			boolean is_shown_in_index, boolean is_rating_locked, boolean has_children, boolean is_pending, boolean is_held, boolean is_note_locked,
			int score, int last_noted_at, int last_commented_at){
		this.file = file;
		this.preview = preview;
		this.sample = sample;

		this.id = id;
		this.parent_id = parent_id;
		this.change = change;

		this.tags = tags;

		this.creator_id = creator_id;
		this.approver_id = approver_id;
		this.author = author;
		this.source = source;
		this.md5 = md5;
		this.rating = rating;
		this.status = status;

		this.created_at = created_at;
		this.updated_at = updated_at;

		this.is_shown_in_index = is_shown_in_index;
		this.is_rating_locked = is_rating_locked;
		this.has_children = has_children;
		this.is_pending = is_pending;
		this.is_held = is_held;
		this.is_note_locked = is_note_locked;
		this.score = score;
		this.last_noted_at = last_noted_at;
		this.last_commented_at = last_commented_at;
	}
	
	public File getFile(){
		return file;
	}

	public Preview getPreview(){
		return preview;
	}

	public Sample getSample(){
		return sample;
	}

	public long getId(){
		return id;
	}

	public long getParentId(){
		return parent_id;
	}

	public long getChange(){
		return change;
	}

	public String[] getTags(){
		return tags;
	}

	public String getCreatorId(){
		return creator_id;
	}

	public String getApproverId(){
		return approver_id;
	}

	public String getAuthor(){
		return author;
	}

	public String getSource(){
		return source;
	}

	public String getMD5(){
		return md5;
	}

	public String getRating(){
		return rating;
	}

	public String getStatus(){
		return status;
	}

	public Date getCreatedAt(){
		return created_at;
	}

	public Date getUpdatedAt(){
		return updated_at;
	}

	public boolean getIsShownInIndex(){
		return is_shown_in_index;
	}

	public boolean getIsRatingLocked(){
		return is_rating_locked;
	}

	public boolean getHasChildren(){
		return has_children;
	}

	public boolean getIsPending(){
		return is_pending;
	}

	public boolean getIsHeld(){
		return is_held;
	}

	public boolean getIsNoteLocked(){
		return is_note_locked;
	}

	public int getScore(){
		return score;
	}

	public int getLastNotedAt(){
		return last_noted_at;
	}

	public int getLastCommentedAt(){
		return last_commented_at;
	}
}
