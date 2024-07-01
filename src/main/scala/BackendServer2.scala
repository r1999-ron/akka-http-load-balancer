object BackendServer2 {
  def main(args: Array[String]): Unit = {
    BackendServer.start(8082, "Response from server 2")
  }
}
