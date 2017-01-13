require 'xmlrpc/client'
server = XMLRPC::Client.new("localhost", "/xmlrpc", 37890)
File.foreach('data/FlopHands.txt') {
  |x|
  words = x.split(' ')
  hand = words[0]+words[1]
  board = words[2]+words[3]+words[4]
  print hand,board," "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "2x[3x-7x],3x[4x-7x],4x[5x-6x],3x2y,4x[2y-3y],5x[2y-4y],6x[2y-5y],7x[2y-4y],8x[2y-3y]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "2x[8x-Tx],3x[8x-9x],4x[7x-9x],7x5y,8x[4y-5y],9x[2y-5y],Tx[2y-5y],Fx[2y-3y]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "3xTx,4xTx,5x[6x-Tx],6x[7x-Tx],7x[8x-9x],8x9x,7x6y,8x[6y-7y],9x[6y-8y],Tx[6y-8y]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "22,Jx[2x-6x],Qx[2x-5x],2xKx,Jx[4y-7y],Qx[2y-7y],Kx[2y-4y]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "Tx[7x-9x],Jx[7x-Tx],Qx[6x-9x],Tx9y,Jx[8y-Ty],Qx[8y-Jy]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "33,44,55,Kx[3x-8x],Ax[2x-6x],Kx[5y-9y],Ax[2y-8y]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "66,77,Qx[Tx-Jx],Kx[9x-Qx],Ax[7x-Kx],Kx[Ty-Qy],Ax[9y-Ky]"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  a = server.call("PPTServer.computeEquityAuto", "holdem", board, "", "generic", [hand, "88,99,TT,JJ,QQ,KK,AA"], 600000, 10, 1)
  print a.lines.first.split(" ")[2].chomp('%'), " "
  print("\n")  
}

# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# a = server.call("PPTServer.computeEquityAuto", "holdem", "AsKhJd", "", "generic", ["Ad4c", "13%"], 600000, 10, 1)
# print a.lines.first.split(" ")[2], " "
# print("\n")

