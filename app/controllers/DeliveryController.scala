package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

case class CreateDeliverForm(name:String,cost:Int,description:String)
case class UpdateDeliverForm(id:Int,name:String,cost:Int,description:String)

@Singleton
class DeliveryController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,deliverRepo:DeliveryRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Delivery controller*/
  val deliverForm: Form[CreateDeliverForm] = Form{
    mapping(
      "name" -> nonEmptyText,
      "cost" -> number,
      "description" ->nonEmptyText)(CreateDeliverForm.apply)(CreateDeliverForm.unapply)
  }
  val updateDeliverForm: Form[UpdateDeliverForm] = Form{
    mapping(
      "id"  -> number,
      "name" -> nonEmptyText,
      "cost" -> number,
      "description" ->nonEmptyText)(UpdateDeliverForm.apply)(UpdateDeliverForm.unapply)
  }

  def getDelivery = Action.async{ implicit request =>
    deliverRepo.list().map(
      delivers => Ok(views.html.delivers(delivers))
    )
    //Ok("Delivery" )
  }
  def getDeliverById(deliverId:Int) = Action.async{ implicit request =>
    deliverRepo.getById(deliverId).map(
      deliver => deliver match{
        case Some(i) => Ok(views.html.delivers(Seq[Delivery](i)))
        case None => Ok("Brak rodzaju dowozu o podanym id")
      }
    )

  }
  def createDelivery =Action.async{implicit request =>
    deliverForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.deliveradd(errorForm)))
      },
      deliver =>{
        deliverRepo.create(deliver.name,deliver.cost,deliver.description).map(_=>
          Redirect(routes.DeliveryController.getDelivery()).flashing("success"->"basket.created")
        )
      }
    )
  }

  def updateDelivery(deliverId: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val deliver = deliverRepo.getById(deliverId)
    deliver.map(b=>{
      val bForm = updateDeliverForm.fill(UpdateDeliverForm(b.head.id,b.head.name,b.head.cost,b.head.description))
      Ok(views.html.deliverupdate(bForm))
    })
  }
  def updateDeliverHandle = Action.async{implicit request=>
    updateDeliverForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.deliverupdate(errorForm))
        )
      },
      deliver =>{
        deliverRepo.update(deliver.id,Delivery(deliver.id,deliver.name,deliver.cost,deliver.description)).map{
          _ => Redirect(routes.DeliveryController.updateDelivery(deliver.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteDelivery(userId: Int) = Action{
    deliverRepo.delete(userId)
    Redirect("/delivers")
  }
}
