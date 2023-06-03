package woowacourse.shopping.data.datasource.order

import woowacourse.shopping.domain.model.OrderRequest
import woowacourse.shopping.domain.model.OrderResponse

interface OrderDataSource {
    fun orderProducts(
        token: String,
        orderRequest: OrderRequest,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    )

    fun requestOrders(
        token: String,
        onSuccess: (List<OrderResponse>) -> Unit,
        onFailure: () -> Unit,
    )

    fun requestSpecificOrder(
        token: String,
        orderId: String,
        onSuccess: (OrderResponse) -> Unit,
        onFailure: () -> Unit,
    )
}
