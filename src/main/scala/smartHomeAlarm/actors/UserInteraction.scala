package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object UserInteraction:
  import smartHomeAlarm.smartHomeAlarmProtocol.*

  enum Command:
    case WaitPin()
    case SendPin(pin: String)

  export Command.*

  def apply(replyTo: ActorRef[TryPin]): Behavior[Command] = 
  Behaviors.setup { context =>
    context.self ! WaitPin()
    active(replyTo)
  }

  private def active(replyTo: ActorRef[TryPin]):Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case WaitPin() =>

          startAsyncKeyboardReading(context)
          Behaviors.same
        case SendPin(pin) =>
          replyTo ! TryPin(pin)
          context.self ! WaitPin()
          Behaviors.same


  def startAsyncKeyboardReading(context: ActorContext[Command]): Unit =
    Future {
      val input = StdIn.readLine()
      if input != null then
        // Invia il testo letto all'attore stesso in modo thread-safe
        context.self ! SendPin(input)
    }