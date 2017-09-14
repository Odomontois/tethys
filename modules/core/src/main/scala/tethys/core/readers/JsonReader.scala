package tethys.core.readers

import tethys.core.commons.LowPriorityInstance
import tethys.core.readers.instances.{ComplexReaders, BasicReaders}
import tethys.core.readers.tokens.TokenIterator

trait JsonReader[A] {
  self =>

  def read(it: TokenIterator)(implicit fieldName: FieldName): Either[ReaderError, A]

  def map[B](fun: A => B): JsonReader[B] = new JsonReader[B] {
    override def read(it: TokenIterator)(implicit fieldName: FieldName): Either[ReaderError, B] = {
      self.read(it) match {
        case Right(a) => ReaderError.catchNonFatal(fun(a))
        case left => left.asInstanceOf[Either[ReaderError, B]]
      }
    }
  }
}

object JsonReader
  extends BasicReaders
    with ComplexReaders
    with LowPriorityJsonReaders {
  def apply[A](implicit jsonReader: JsonReader[A]): JsonReader[A] = jsonReader
}

private[readers] trait LowPriorityJsonReaders {
  implicit final def lowPriorityReader[A](implicit lowPriorityInstance: LowPriorityInstance[JsonReader[A]]): JsonReader[A] = {
    lowPriorityInstance.instance
  }
}
