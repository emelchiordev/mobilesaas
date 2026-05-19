package re.melchior.saviomobile.data.local.objectbox

import android.content.Context
import io.objectbox.Box
import io.objectbox.BoxStore

object ObjectBoxStore {
    private var _store: BoxStore? = null

    val store: BoxStore
        get() = checkNotNull(_store) { "ObjectBoxStore.init() must be called first" }

    fun init(context: Context) {
        if (_store != null) return
        _store = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }

    fun articlesBox(): Box<TenantArticleBox> = store.boxFor(TenantArticleBox::class.java)
}
