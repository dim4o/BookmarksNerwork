package bg.jwd.bookmarks.services.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import bg.jwd.bookmarks.dao.KeywordDao;
import bg.jwd.bookmarks.entities.Keyword;
import bg.jwd.bookmarks.services.generic.AbstractService;
import bg.jwd.bookmarks.servises.KeywordService;

public class KeywordsServiceImpl extends AbstractService<Keyword> implements KeywordService{

	@Autowired
	KeywordDao keywordDao;

	/*@Transactional
	@Override
	public void addAll(Set<Keyword> keywords) {
		this.keywordDao.addAll(keywords);
	}*/
}
