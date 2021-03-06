package bg.jwd.bookmarks.services.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import bg.jwd.bookmarks.dao.BookmarkDao;
import bg.jwd.bookmarks.dao.KeywordDao;
import bg.jwd.bookmarks.dao.TagDao;
import bg.jwd.bookmarks.dao.UrlDao;
import bg.jwd.bookmarks.dao.UserDao;
import bg.jwd.bookmarks.dto.AddBookmarkFormDto;
import bg.jwd.bookmarks.dto.UserTagDto;
import bg.jwd.bookmarks.entities.Bookmark;
import bg.jwd.bookmarks.entities.Keyword;
import bg.jwd.bookmarks.entities.Tag;
import bg.jwd.bookmarks.entities.Url;
import bg.jwd.bookmarks.entities.User;
import bg.jwd.bookmarks.enums.VisibilityType;
import bg.jwd.bookmarks.security.CurrentUser;
import bg.jwd.bookmarks.services.generic.AbstractService;
import bg.jwd.bookmarks.servises.BookmarkService;
import bg.jwd.bookmarks.util.UserUtils;

@Service
public class BookmarkServiceImpl extends AbstractService<Bookmark> implements BookmarkService {
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	BookmarkDao bookmarkDao;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	private KeywordDao keywordDao;
	
	@Autowired
	private TagDao tagDao;
	
	@Autowired
	private UrlDao urlDao;
	
	@Transactional
	@Override
	public Bookmark createBookmark(Bookmark bookmarkToAdd, AddBookmarkFormDto addBookmarkFormDto) throws Exception{
		//Session session = this.sessionFactory.getCurrentSession();
		String title = addBookmarkFormDto.getTitle();
		Url url = new Url(addBookmarkFormDto.getLink());
		String description = addBookmarkFormDto.getDescription();
		Set<Keyword> keywords = this.mapToKeyordList(addBookmarkFormDto.getKeywords());
		Set<Tag> tags = this.mapToTagList((addBookmarkFormDto.getTags()));
		User author = UserUtils.getCurrentUser().getUser();
		VisibilityType visibility = VisibilityType.valueOf(addBookmarkFormDto.getVisibility());
		
		this.keywordDao.addAll(keywords);
		this.tagDao.addAll(tags);
		urlDao.addIfNotExists(url);
		
		Set<Tag> tagsFromDb = new HashSet<Tag>();
		for (Tag tag : tags) {
			Tag t = tagDao.getByProperty("tagName", tag.getTagName());
			tagsFromDb.add(t);
		}

		author.getTags().addAll(tagsFromDb);
		userDao.update(author);

		bookmarkToAdd = new Bookmark(title, url, author, visibility);
		bookmarkToAdd.setDescription(description);
		bookmarkToAdd.setKeywords(keywords);
		bookmarkToAdd.setTags(tags);
			
		return bookmarkToAdd;
	}
	
	@Transactional
	@Override
	public Bookmark editBookmark(Bookmark bookmark, AddBookmarkFormDto form){
		Session s = this.sessionFactory.getCurrentSession();
		User author = userDao.get(UserUtils.getCurrentUser().getUser().getUserId());
		Url url = new Url(form.getLink());
		Url urlFromDb = this.urlDao.getByProperty("link", form.getLink());
		Set<Keyword> keywords = this.mapToKeyordList(form.getKeywords());
		Set<Tag> tags = this.mapToTagList(form.getTags());

		bookmark.setTitle(form.getTitle());
		bookmark.setDescription(form.getDescription());
		bookmark.setVisibility(VisibilityType.valueOf(form.getVisibility()));
		
		// TODO: not working
		if(urlFromDb != null && !urlFromDb.getBookmarks().contains(bookmark)){
			bookmark.setVisibility(VisibilityType.Private);
		}
			
			//urlDao.add(url);
		

		keywordDao.addAll(keywords);
		tagDao.addAll(tags);
		url = urlDao.addIfNotExists(url);
		
		bookmark.setUrl(url);
		
		Set<Keyword> keysFromDb = new HashSet<Keyword>();
		for (Keyword keyword : keywords) {
			Keyword k = keywordDao.getByProperty("keyword", keyword.getKeyword());
			keysFromDb.add(k);
		}
		
		Set<Tag> tagsFromDb = new HashSet<Tag>();
		for (Tag tag : tags) {
			Tag t = tagDao.getByProperty("tagName", tag.getTagName());
			tagsFromDb.add(t);
		}

		author.getTags().addAll(tagsFromDb);
		s.persist(author);
		bookmark.setKeywords(keysFromDb);
		bookmark.setTags(tagsFromDb);
		//this.userDao.update(bookmark.getAuthor());
		
		return bookmark;
	}
	
	private Set<Keyword> mapToKeyordList(String wordsSequence){
		Set<Keyword> result = new HashSet<Keyword>();
		if(wordsSequence != null && !wordsSequence.isEmpty()){
			List<String> sequenseAsList = Arrays.asList(wordsSequence.trim().split("\\s*,\\s*"));
			
			for (String item : sequenseAsList) {
				Keyword itemToAdd = new Keyword(item);
				result.add(itemToAdd);
			}
		}

		return result;
	}
	
	private Set<Tag> mapToTagList(String wordsSequence){
		Set<Tag> result = new HashSet<Tag>();
		if(wordsSequence != null && !wordsSequence.isEmpty()){
			List<String> sequenseAsList = Arrays.asList(wordsSequence.trim().split("\\s*,\\s*"));
			
			for (String item : sequenseAsList) {
				Tag itemToAdd = new Tag(item);
				result.add(itemToAdd);
			}
		}
		
		return result;
	}
	
	/*private <T> Set<T> mapToList(String wordsSequence){
		Set<T> result = new HashSet<T>();
		Class<T> clazz = null;
		if(wordsSequence != null && !wordsSequence.isEmpty()){
			List<String> sequenseAsList = Arrays.asList(wordsSequence.trim().split("\\s*,\\s*"));
			
			for (String item : sequenseAsList) {
				Constructor constructor = clazz.getConstructor(String.class);
				T itemToAdd = (T) constructor.newInstance(item);
			}
		}
		
		return result;
	}*/
	
	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Bookmark> getUserBookmarksWithPagination(int page, int pageSize, String username, boolean publicFilter){

		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Bookmark.class)
				.createAlias("author", "a");
		criteria.add(Restrictions.like("a.username", username));
		if(publicFilter){
			criteria.add(Restrictions.like("visibility", VisibilityType.Public));
		}
		criteria
			.addOrder(Order.desc("creationDate"))
			.addOrder(Order.asc("title"));
		
		//criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		//criteria.setFetchMode("lineItems", FetchMode.JOIN);
		//criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		List<Bookmark> result = criteria.list();

		return result;
	}
	
	@Transactional
	@Override
	public long getCount(String username){
		Criteria criteria = this.sessionFactory.getCurrentSession()
				.createCriteria(Bookmark.class)
				.createAlias("author", "a");
		criteria.add(Restrictions.like("a.username", username));
		
		return  (long)criteria.setProjection(Projections.rowCount()).uniqueResult();
	}

	/**
	 * Deletes bookmark
	 * Deletes url only if hasn't other bookmarks associated
	 */
	@Transactional
	@Override
	public boolean safeDeleteBookmark(Bookmark bookmarkToDelete) {
		Url url = bookmarkToDelete.getUrl();
		url.getBookmarks().remove(bookmarkToDelete);
		boolean bookmarkDeleted = this.delete(bookmarkToDelete);
		
		if(url.getBookmarks().size() == 0){
			urlDao.delete(url);
		}
		
		return bookmarkDeleted;
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Bookmark> getAllUserBookmarks(String username) {
		
			Criteria criteria = this.sessionFactory.getCurrentSession()
					.createCriteria(Bookmark.class)
					.createAlias("author", "a");
			criteria.add(Restrictions.like("a.username", username));
			List<Bookmark> result =  criteria.list();

		return result;
	}
	
	@Transactional
	@Override
	public void importBookmarks(HttpServletRequest request, MultipartFile file, String visibility) throws Throwable  {
		File uploadedFile = this.uploadFile(request, file);

		String html = null;
        
		try{
			html = Files.toString(uploadedFile, Charsets.UTF_8);}
		catch(Exception e){
			e.printStackTrace();
		}
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("a[href]");

			
		for (Element element : links) {
			String link = element.attr("href");
			String title = element.text();
			if(link != null && !link.isEmpty() && title != null && !title.isEmpty()){
				
				Url url = new Url(link);
				Bookmark currentBookmark = new Bookmark(
						title, 
						url, 
						UserUtils.getCurrentUser().getUser(), 
						VisibilityType.Public);
				Url urlFromDb = urlDao.getByProperty("link", link);
				
				if(urlFromDb != null){
					currentBookmark.setUrl(urlFromDb);
					currentBookmark.setVisibility(VisibilityType.Private);
				} else {
					urlDao.add(url);
				}
				
				bookmarkDao.add(currentBookmark);
			}
		}
	}
	
	private File uploadFile(HttpServletRequest request, MultipartFile file) {
		// Root Directory.
        String uploadRootPath = request.getSession().getServletContext().getRealPath("upload");
        File uploadRootDir = new File(uploadRootPath);

        // Create directory if it not exists.
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }

        // Client File Name
        String name = file.getOriginalFilename();
        File uploadedFile = null;
 
        if (name != null && name.length() > 0) {
            try {
                byte[] bytes = file.getBytes();
 
                // Create the file on server
                File serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + name);
 
                // Stream to write data to file in server.
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();
                uploadedFile = serverFile;
	        } catch (Exception e) {
	            System.out.println("Error Write file: " + name);
	            // TODO: Error handling
	        }
        }
        
		return uploadedFile;
	}

	@Transactional
	@Override
	public List<Bookmark> getUserBookmarksByTag(String username, String tagName) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria cr = session.createCriteria(Bookmark.class)
				.createAlias("tags", "tagsAlias");
				
		cr.add(Restrictions.eq("tagsAlias.tagName", tagName))
				.createAlias("author", "authorAlias");
		
		cr.add(Restrictions.eq("authorAlias.username", username));

		
		return cr.list();
	}
	
	@Transactional
	@Override
	public List<UserTagDto> getUserTagsByBookmarksCount(long userId) {
		Session session = this.sessionFactory.getCurrentSession();

		String sql = 
				"SELECT tag_name, COUNT(*)\n" +
				"FROM tags t\n" + 
					"JOIN bookmarks_tags bt\n" + 
						"ON t.tag_id = bt.tag_id\n" + 
					"JOIN bookmarks b\n" + 
						"ON bt.bookmark_id = b.bookmark_id\n" + 
					"JOIN USERS u\n" +
						"ON b.author_id = u.user_id\n" +
					"WHERE u.user_id = " + userId + "\n" +
				"GROUP BY t.tag_name, tag_name";
		
		Query query = session.createSQLQuery(sql);
		@SuppressWarnings("unchecked")
		List<Object[]> tuples = query.list();
		List<UserTagDto> tagsByCount = new ArrayList<>();
		
		for (Object[] tuple : tuples) {
			String tagName = (String)tuple[0];
			BigDecimal count = (BigDecimal)tuple[1];
			tagsByCount.add(new UserTagDto(tagName, count));
		}
		
		return tagsByCount;
	}
}
