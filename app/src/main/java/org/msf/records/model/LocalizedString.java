package org.msf.records.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

public final class LocalizedString {
	private final ImmutableMap<String, String> mTranslationByLocale;

	private LocalizedString(Builder builder) {
		mTranslationByLocale = ImmutableMap.copyOf(builder.mTranslationByLocale);
		Preconditions.checkNotNull(mTranslationByLocale, "mTranslationByLocale");
	}

	public boolean isEmpty() {
		return mTranslationByLocale.isEmpty();
	}

	public ImmutableCollection<String> getLocales() {
		return mTranslationByLocale.keySet();
	}

	public String getTranslationForLocale(String locale) {
		return mTranslationByLocale.get(locale);
	}

	public Map<String, String> asMap() {
		return mTranslationByLocale;
	}

    public static final class Builder {
    	private final Map<String, String> mTranslationByLocale = new HashMap<>();

    	public void addTranslation(String locale, String translation) {
    		Preconditions.checkNotNull(locale);
    		Preconditions.checkNotNull(translation);
    		mTranslationByLocale.put(locale, translation);
    	}

    	public LocalizedString build() {
    		return new LocalizedString(this);
    	}
    }
}