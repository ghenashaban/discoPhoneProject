package service

import model._

trait Promotion {

  def applyPromotion(callWithCost: List[CallWithCost]): List[Cost]
}

class ApplyPromotion extends Promotion {

  def applyPromotion(callWithCost: List[CallWithCost]): List[Cost] = {
    val costListPerCustomer: List[Double] = callWithCost.groupBy(_.call.phoneNumberCalled).map{case(_, value) =>
    value.map(_.cost.value).sum
    }.toList

    val listOfCostsPerCustomerFiltered = if (callWithCost.length > 1){
      costListPerCustomer.sorted.reverse.tail
    } else {
      costListPerCustomer
    }

    listOfCostsPerCustomerFiltered.map(Cost)

  }

}




