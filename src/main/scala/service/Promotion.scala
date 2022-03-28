package service

import model._

trait Promotion {

  def applyPromotion(callWithCost: List[CallWithCost]): List[Cost]
}

class ApplyPromotion extends Promotion {

  def applyPromotion(callWithCost: List[CallWithCost]): List[Cost] = {

    val costListPerCustomer = callWithCost.groupBy(_.call.phoneNumberCalled).view.mapValues(_.map(_.cost.value).sum).values.toList

    val listOfCostsPerCustomerFiltered = if (callWithCost.length > 1){
      costListPerCustomer.sorted.reverse.tail
    } else {
      costListPerCustomer
    }
    listOfCostsPerCustomerFiltered.map(Cost)
  }
}




