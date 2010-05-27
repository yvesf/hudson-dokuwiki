package org.xapek.dokuwiki;

import org.apache.commons.collections.Transformer;

public abstract class TypedTransformer<From, To> implements Transformer {

	@SuppressWarnings("unchecked")
	public Object transform(Object input) {
		return typedTransform((From) input);
	}

	public abstract To typedTransform(From input);
}
