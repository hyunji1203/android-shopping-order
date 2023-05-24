package woowacourse.shopping.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import woowacourse.shopping.domain.model.Cart
import woowacourse.shopping.domain.model.CartProduct
import woowacourse.shopping.domain.model.ProductCount
import woowacourse.shopping.domain.model.page.Page
import woowacourse.shopping.domain.model.page.Pagination
import woowacourse.shopping.domain.repository.CartRemoteRepository
import woowacourse.shopping.domain.repository.ProductRepository
import woowacourse.shopping.mapper.toDomain
import woowacourse.shopping.mapper.toUi
import woowacourse.shopping.model.UiCartProduct
import woowacourse.shopping.model.UiProduct
import woowacourse.shopping.ui.cart.CartContract.Presenter
import woowacourse.shopping.ui.cart.CartContract.View

class CartPresenter(
    view: View,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRemoteRepository,
    cartSize: Int = 5,
) : Presenter(view) {
    private var cart: Cart = Cart(minProductSize = 1)
    private var currentPage: Page = Pagination(sizePerPage = cartSize)

    private val _totalCheckSize = MutableLiveData(cartRepository.getAllCartProduct().size)
    val totalCheckSize: LiveData<Int> get() = _totalCheckSize

    private val _pageCheckSize = MutableLiveData(currentPage.getCheckedProductSize(cart))
    val isAllChecked: LiveData<Boolean> = Transformations.map(_pageCheckSize) { pageCheckSize ->
        pageCheckSize == currentPage.takeItems(cart).size
    }

    override fun fetchCart(page: Int) {
        currentPage = currentPage.update(page)
        cart = cart.update(loadCartProducts())

        view.updateNavigatorEnabled(currentPage.hasPrevious(), currentPage.hasNext(cart))
        view.updatePageNumber(currentPage.toUi())
        fetchView()
    }

    private fun loadCartProducts(): List<CartProduct> = cartRepository.getAllCartProduct()
//            .mapNotNull {
//            val product = productRepository.findProductById(it.productId)
//            product?.run { CartProduct(it.id, this, ProductCount(it.count), it.checked) }
//        }

    override fun changeProductCount(cartProduct: UiCartProduct, count: Int, increase: Boolean) {
        val newCart = changeCount(cartProduct.product, count, increase)
        newCart.findCartProductById(cartProduct.id)?.let {
            cartRepository.updateProductCountById(it.id, it.selectedCount)
        }
        updateCart(newCart)
    }

    private fun changeCount(product: UiProduct, count: Int, isInc: Boolean): Cart = when (isInc) {
        true -> cart.increaseProductCount(product.toDomain(), count)
        false -> cart.decreaseProductCount(product.toDomain(), count)
    }

    override fun changeProductSelectState(cartProduct: UiCartProduct, isSelect: Boolean) {
        updateCart(changeSelectState(cartProduct.product, isSelect))
    }

    private fun changeSelectState(product: UiProduct, isSelect: Boolean): Cart =
        if (isSelect) cart.select(product.toDomain()) else cart.unselect(product.toDomain())

    override fun toggleAllCheckState() {
//        updateCart(
//            if (isAllChecked.value == true) {
//                cart.unselectAll(currentPage)
//            } else cart.selectAll(
//                currentPage
//            )
//        )
    }

    override fun removeProduct(cartProduct: UiCartProduct) {
        cartRepository.deleteCartProductById(cartProduct.id)
        fetchCart(currentPage.value)
    }

    override fun order() {
        if (_totalCheckSize.value == 0) {
            view.showOrderFailed(); return
        }
        cart.items.forEach { cartRepository.deleteCartProductById(it.id) }
        view.showOrderComplete(_totalCheckSize.value ?: 0)
    }

    override fun navigateToHome() {
        view.navigateToHome()
    }

    private fun updateCart(newCart: Cart) {
        cart = cart.update(newCart)
//        cartRepository.update(currentPage.takeItems(cart))
        fetchView()
    }

    private fun fetchView() {
//        _totalCheckSize.value = cartRepository.getCheckedProductCount()
        _pageCheckSize.value = currentPage.getCheckedProductSize(cart)
        view.updateTotalPrice(cart.getCheckedProductTotalPrice())
        view.updateCart(currentPage.takeItems(cart).toUi())
    }
}