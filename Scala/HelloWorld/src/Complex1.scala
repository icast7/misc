class Complex1 (real: Double, imaginary: Double){
  def re() = real
  def im() = imaginary
}

object ComplexNumbers{
  def main(args: Array[String]){
    val c = new Complex1(1.2, 3.4)
    println("Imaginary part: " + c.im())
  }
}