import RPi.GPIO as GPIO
import time 
import sys
import threading
import socket
from socket import *
import serial
import Adafruit_DHT as dth
# GUI import 
import pygame as pg
from datetime import datetime

# Thread Name 
warningTh = threading.Thread() # 비상등
keyTh = threading.Thread() # 차 키 위치 찾기
buzzerTh1 = threading.Thread() # 히터,에어컨,front,rear에 쓰일 부저
buzzerTh2 = threading.Thread() # 키찾기위한 알람음, 차 위치 알람음, 삐용삐용

gpsTh = threading.Thread() # 차 위치를 찾기위한 스레드 
frontTh = threading.Thread() # front작동
rearTh = threading.Thread() # rear작동
airTh = threading.Thread()  # 에어컨
heaterTh = threading.Thread() #히터
trunkTh = threading.Thread() # 트렁크서보모터
doorTh = threading.Thread() # 차 문 서보모터

# Mutex Name
buzzerLock1 = threading.Lock()
buzzerLock2 = threading.Lock()
ledLock = threading.Lock()
servoLock1 = threading.Lock() # 차 문 열고 닫기가 있기 때문에
servoLock2 = threading.Lock() # 트렁크 문 열고 닫기가 있기 때문에

# GPIO PIN init
LED1 = 18
LED2 = 3
BUZZER1 = 19
BUZZER2 = 26
TRUNK = 16
DOOR = 25
# 계이름
do = 262
re = 294
mi = 330
mi2 =659
pa = 349
sol =392
sol2 = 831
ra = 440
si = 494
do2 = 523
note1 =[do,mi,sol,do2]
note2 =[do2,ra,do2,ra]
note3 =[sol2,mi2]
note4 =[do2,sol,mi,do]
# GUI 변수
x = 0
x_step = 0
x_break = 0
km = 0 # 주행거리
kmh = 0 # 주행속도
timerSpeed =500 #타이머 속도
recv_data ="" # 클라이언트로부터 전송받은 문
def GPIOsetup():
	global LED1,LED2,BUZZER1,BUZZER2,TRUNK,DOOR
	# GPIO init
	GPIO.setmode(GPIO.BCM)
	GPIO.setwarnings(False)
	GPIO.setup(LED1,GPIO.OUT)
	GPIO.setup(LED2,GPIO.OUT)
	GPIO.setup(BUZZER1,GPIO.OUT)
	GPIO.setup(BUZZER2,GPIO.OUT)
	GPIO.setup(TRUNK,GPIO.OUT)
	GPIO.setup(DOOR,GPIO.OUT)
	
def trunkfunction(recv_data):
	print("trunk:",recv_data)
	global TRUNK
	p = GPIO.PWM(TRUNK,50)
	try:
		p.start(0)
		if recv_data == 't':
			print("t들어왔어")
			p.ChangeDutyCycle(12)
			time.sleep(1)
			p.stop()
		if recv_data == 'T':
			p.ChangeDutyCycle(7.5)
			time.sleep(1)
			p.stop()
	except AttributeError as e:
		print("trunkfunction ERROR",e)
		p.stop()
	GPIO.cleanup()
	GPIOsetup()	
	return
	
def doorfunction(recv_data):
	print("door")
	global DOOR
	p = GPIO.PWM(DOOR,50)
	try:
		p.start(0)
		if recv_data == 'o':
			print("o들어왓어")
			p.ChangeDutyCycle(12)
			time.sleep(1)
			p.stop()
		if recv_data == 'c':
			p.ChangeDutyCycle(7.5)
			time.sleep(1)
			p.stop()
	except AttributeError as e :
		print("doorfunction ERROR",e)
		p.stop()
	
	GPIO.cleanup()
	GPIOsetup()	
	return

def warningfunction(recv_data):
	print("warning:",recv_data)
	global ledLock,LED1,LED2
	if(not ledLock.acquire(False)):
		return
	try:
		if(recv_data == 'l'):
			GPIO.output(LED1,True)
			GPIO.output(LED2,True)
		elif(recv_data == 'L'):
			GPIO.output(LED1,False)
			GPIO.output(LED2,False)
		else:
			print("warningfunction recv_data ERROR")
			print("ERROR warning:",recv_data)
	except:
		print("warningfunction exception ERROR")
		ledLock.release()
		GPIO.cleanup()
		GPIOsetup()
		return
		
	ledLock.release()
	GPIO.cleanup()
	GPIOsetup()
	return

def gpsfunction():
	global ledLock,buzzerLock2,LED1,LED2,BUZZER2,note2
	if(not ledLock.acquire(False)):
		print("acuire")
		return
	if(not buzzerLock2.acquire(False)):
		print("acuire1")
		return
	p = GPIO.PWM(BUZZER2,500)
	try:
		for i in note2[0:]:
			print("g for문")
			p.start(99)
			GPIO.output(LED1,True)
			GPIO.output(LED2,True)
			p.ChangeFrequency(i)
			time.sleep(0.5)
			GPIO.output(LED1,False)
			GPIO.output(LED2,False)
			p.stop()

	except AttributeError as e:
		print(e)
		GPIO.cleanup()
		GPIOsetup()
		return
	p.stop()
	buzzerLock2.release()
	ledLock.release()
	GPIO.cleanup()
	GPIOsetup()

def keyfunction():
	global buzzerLock2,BUZZER2,note3
	if(not buzzerLock2.acquire(False)):
		return
	p = GPIO.PWM(BUZZER2,500)
	try:
		for i in note3[0:]:
			print("key for문")
			p.start(99)
			p.ChangeFrequency(i)
			time.sleep(0.5)
			p.stop()
	except AttributeError as e:
		print(e)
		p.stop()
		buzzerLock2.release()
		GPIO.cleanup()
		GPIOsetup()
		return
	buzzerLock2.release()
	GPIO.cleanup()
	GPIOsetup()
	return

def dingdongfunction(number):
	print("number ?",number)
	global buzzerLock1,BUZZER1,note1,note4
	if(not buzzerLock1.acquire(False)):
		return
	p = GPIO.PWM(BUZZER1,500)
	try:
		if(number == 1):
			for i in note1[0:]:
				p.start(99)
				p.ChangeFrequency(i)
				time.sleep(0.3)
				p.stop()
			buzzerLock1.release()
		if(number == 2):
			for i in note4[0:]:
				p.start(99)
				p.ChangeFrequency(i)
				time.sleep(0.3)
				p.stop()
			buzzerLock1.release()
	except AttributeError as e:
		print(e)
		p.stop()
		buzzerLock1.release()
		GPIO.cleanup()
		GPIOsetup()
		return
	GPIO.cleanup()
	GPIOsetup()
	return

def GPIOControl(data):
	print("data?",data)
	try:
		if(data == 'l' or data =='L'):
			if(data =='l'):
				warningTh = threading.Thread(target = warningfunction,args=('l',))
			if(data == 'L'):
				warningTh =threading.Thread(target = warningfunction,args=('L',))
			warningTh.start()
			warningTh.join()
		elif(data =='o' or data == 'c'):
			if(data =='o'):
				doorTh = threading.Thread(target = doorfunction,args=('o',))
			if(data =='c'):
				doorTh = threading.Thread(target = doorfunction,args=('c',))
			doorTh.start()
			doorTh.join()
		elif(data =='t' or data == 'T'):
			if (data == 't'):
				print("t?")
				trunkTh = threading.Thread(target = trunkfunction,args=('t',))
			if(data == 'T'):
				trunkTh = threading.Thread(target = trunkfunction,args=('T',))
			trunkTh.start()
			trunkTh.join()
		elif(data =='a' or data == 'A'):
			if(data == 'a'):
				airTh = threading.Thread(target = dingdongfunction,args=(1,))
			if(data =='A'):
				airTh = threading.Thread(target =dingdongfunction,args=(2,))
			airTh.start()
			airTh.join()
		elif(data =='h' or data == 'H'):
			if(data == 'h'):
				heaterTh = threading.Thread(target = dingdongfunction,args=(1,))
			if(data == 'H'):
				heaterTh = threading.Thread(target = dingdongfunction,args=(2,))
			heaterTh.start()
			heaterTh.join()
		elif(data =='g'):
			gpsTh = threading.Thread(target=gpsfunction)
			gpsTh.start()
			gpsTh.join()
		elif(data =='k'):
			keyTh = threading.Thread(target = keyfunction)
			keyTh.start()
			keyTh.join()
		elif(data == 'r' or data =='R'):
			if (data == 'r'):
				rearTh = threading.Thread(target = dingdongfunction, args=(1,))
			if(data == 'R'):
				rearTh = threading.Thread(target = dingdongfunction,args=(2,))
			rearTh.start()
			rearTh.join()
		elif(data =='f' or data =='F'):
			if(data =='f'):
				frontTh = threading.Thread(target = dingdongfunction,args=(1,))
			if(data =='F'):
				frontTh = threading.Thread(target = dingdongfunction,args=(2,))
			frontTh.start()
			frontTh.join()
	except:
		return
		
class socket_server():
	
	def __init__(self):
		self.server_ip="192.168.1.228"
		self.server_port= 7000
		self.server_ready =False
		self.client_handler = None
		
		self.server_init()
		
		self.client_waiter = threading.Thread(target=self.client_wait,args=())
		self.client_waiter.start()
		
	def server_init(self):
		print("server Init")
		
		self.server_socket = socket(AF_INET,SOCK_STREAM)
		self.server_socket.setsockopt(SOL_SOCKET,SO_REUSEADDR,1)
		self.server_socket.bind((self.server_ip,self.server_port))
		self.server_socket.listen(5)
		
		print("done : server ready")
		self.server_ready = True
		
		 
	def client_wait(self):
		print("waiting client..")
		try:
			while True:
				while self.server_ready is False :
					pass
				
				client_sock , client_addr = self.server_socket.accept()
				self.client_handler = client_handler(client_sock,client_addr)
				self.client_connected = True
		except KeyboardInterrupt as e:
			print("Bye bye!",e)
			return
			
class client_handler():
	
	def __init__(self,client_sock,client_addr):
		self.client_socket = client_sock
		self.client_addr = client_addr
		self.server_msg = ""
		self.recv_msg =""
		self.kmh = 0
		self.axelonoff ="off"
		self.breakonoff ="off"
		self.km = 0
		self.axelTh = threading.Thread()
		self.breakTh=threading.Thread()
		self.ultraLock =threading.Lock() # kmh 경쟁조건 
		
		self.receiver = threading.Thread(target=self. client_recv, args=( ))
		self.receiver.start()
		self.drivetime = "0,0,0"
		self.sender = threading.Thread(target=self.client_send,args=( ))
		self.sender.start()
		self.axelTh = threading.Thread(target =self.ultraControl,args=())
		self.breakTh = threading.Thread(target =self.ultraControl,args=())
		self.breakTh.start()
		self.breakTh.join()
		self.axelTh.start()
		self.axelTh.join()
		
	def client_recv(self):
		global ultraLock
		print("success connected!")
		try:
			while True:
				self.client_msg = self.client_socket.recv(1024)
				recv_data = self.client_msg.decode("utf-8")
				if(recv_data == 'drivemode'):
					self.drivemode = threading.Thread(target=self.setupGUI,args=())
					self.drivemode.start()
					self.drivemode.join()
		
				GPIOControl(recv_data)
		except :
			print("Bye bye????????????????")
			return
		
	def client_send(self):
		try:
			while True:
				humi,temp = dth.read_retry(dth.DHT11,4)
				#print("Temper ={0:0.1f}*C Humidity={1:0.1f}%".format(humi,temp))
			
				send_data = "{0:0.1f},{1:0.1f},{2},{3},{4},{5},{6}".format(humi,temp,self.km,self.kmh,self.drivetime,self.axelonoff,self.breakonoff)
				self.server_msg = send_data
				'''print("self.break",self.breakonoff)
				print("self.axel",self.axelonoff)'''
				if self.server_msg is not "":
					'''print("client_send :",self.server_msg.encode())'''
					self.client_socket.send(self.server_msg.encode())
					self.server_msg = ""
		except:
			return
				
	def ultraControl(self):		
		Baudrate =9600
		Port ="/dev/ttyUSB0"	
	
		ser = serial.Serial(port=Port,baudrate=Baudrate,timeout=2)
		if(ser.isOpen() == False):
			ser.open()
		ser.flushInput()
		ser.flushOutput()
		try:
	
			while True:
				ser.flushInput()
				ser.flushOutput()
				data = ser.readline()
				if(len(data)>0):
					dedata = data.decode("utf-8")
					if(dedata[0] == "D"):
						axel = dedata[1:dedata.find("/")]
						bre = dedata[dedata.find("/")+1:dedata.find("\n")]
						self.axelControl(axel)
				
						self.breControl(bre)
						'''print("axel :",axel)
						print("break:",bre)'''
						if(self.kmh<0):
							self.kmh = 0
						if(self.kmh>200):
							self.kmh = 180
						if(int(axel)<int(bre)):
							self.breakonoff="off"
							self.axelonoff="axel"
						if(int(bre)<int(axel)):
							self.breakonoff="break"
							self.axelonoff="off"
		except SyntaxError as e:
			print(e)
			return
		finally:
			ser.close()
			return
			
	def breControl(self,bre):
		bre = int(bre)
		if(not self.ultraLock.acquire(False)):
			return
	
		try:
	
			if(bre <10 and bre >30):
				self.kmh  = self.kmh - 5
			elif(bre == 9):
				self.kmh = self.kmh -6
			elif(bre ==8):
				self.kmh = self.kmh -7
			elif(bre == 7):
				self.kmh = self.kmh -8
			elif(bre ==6):
				self.kmh = self.kmh -9
			elif(bre ==5):
				self.kmh = self.kmh -10
			elif(bre ==4):
				self.kmh = self.kmh -15
			elif(bre <4):
				self.kmh = self.kmh -30
			else:
				'''print("넘 멀엉")'''
		except AttributeError as e:
			print(e)
			return
		finally:
			self.ultraLock.release()
			return
			
	def axelControl(self,axel):
		axel = int(axel)
		
		if(not self.ultraLock.acquire(False)):
			return
	
		try:
		
			if(axel <10 and axel >30):
				self.kmh  = self.kmh + 1
			elif(axel == 9):
				self.kmh = self.kmh +2
			elif(axel ==8):
				self.kmh = self.kmh +3
			elif(axel == 7):
				self.kmh = self.kmh +4
			elif(axel ==6):
				self.kmh = self.kmh +5
			elif(axel ==5):
				self.kmh = self.kmh +6
			elif(axel ==4):
				self.kmh = self.kmh +7
			elif(axel <4):
				self.kmh = self.kmh +8
			else:
				'''print("넘멀엉")'''
		except AttributeError as e :
			print(e)
			return
		finally:
			self.ultraLock.release()
			return

	def setupGUI(self):		
		pg.init()
		width,height = 750,960
		screen = pg.display.set_mode((width,height))
		pg.display.set_caption('pyCar Management')
		
		#loadImage
		background = pg.image.load('/root/IOT/finalProject/final/img/backg22.png')
		speedDisplay = pg.image.load('/root/IOT/finalProject/final/img/speedblue.png')
		b1 = pg.image.load('/root/IOT/finalProject/final/img/b1.png')
		b2 = pg.image.load('/root/IOT/finalProject/final/img/b3.png')
		line =pg.image.load('/root/IOT/finalProject/final/img/line.png')
		sky = pg.image.load('/root/IOT/finalProject/final/img/sky3.png')
		car = pg.image.load('/root/IOT/finalProject/final/img/car.png')
		loadImage = pg.image.load('/root/IOT/finalProject/final/img/load.PNG')
		lx,ly = 280,0
		bx1,by1 = 0,100
		bx2,by2 = 170,160
		skyx = 0
		nowmin = 0;
		speedkmh=0
		timeStep = 1000
		#timer init
	
		pg.time.set_timer(pg.USEREVENT,timeStep)
		clock = pg.time.Clock()
		distance = pg.font.SysFont("kopubworldpro.ttf",18) #주행거리
		speed = pg.font.SysFont("kopubworldpro.ttf",32) # 주행속도
		when = pg.font.SysFont("freemono.ttf",18)
		try:
			
			while(True):
				for e in pg.event.get():
					if e.type == pg.QUIT:
						pg.quit()
						exit(0)
					if e.type == pg.USEREVENT:
				
						if(self.kmh <0 ):
							speedkmh = 0
							self.kmh =0
						if(self.axelonoff =="axel"):
							if(self.kmh >100 or self.kmh<180):						
								ly = ly + 15
							elif(self.kmh>50 or self.kmh<100):
								ly = ly + 10
						if(self.breakonoff =="break"):
							if(self.kmh>100 or self.kmh<180):
								ly = ly - 15
							elif(self.kmh>50 or self.kmh<100):
								ly = ly -10
						ly = ly + 5
						bx1,by1 = bx1 - self.kmh , by1+2
						bx2,by2 = bx2 +self.kmh , by2+2
						self.km = self.km +1
						speedkmh = self.kmh + speedkmh
						skyx = skyx -5
					
						if(bx1 < -135 or by1 >114):
							bx1 = 0
							by1 = 100
						if(bx2 > 290 or by2 > 175):
							bx2 = 170
							by2 = 160
						if(self.kmh>70):
							GPIOControl('g')
							print("70")
							
				now = datetime.now()
				nowtime = str(now.hour) +"H  "+str(now.minute) + "M  "+str(now.second)
				self.drivetime ="0,{0},{1}".format(nowmin,now.second)
				if(now.second %60 == 0):
					nowmin = nowmin +1
				clock.tick(30)
				screen.fill((255,255,255))
				kmlabel = distance.render(str(self.km)+"km",True,(80,80,80))
				speedlabel = speed.render(str(self.kmh)+"km/h",True,(0,0,0))
				whenlabel = when.render(str(nowtime),True,(80,80,80))
				
				rect1 = kmlabel.get_rect()
				rect2 = speedlabel.get_rect()
				rect3 = whenlabel.get_rect()
				rect1.center=(115,40)
				rect2.center=(115,60)
				rect3.center=(115,80)
				screen.blit(b1,(bx1,by1))
				screen.blit(b2,(bx2,by2))
				screen.blit(loadImage,(0,0))
				screen.blit(line,(lx,ly))
				screen.blit(background,(0,230))
				screen.blit(sky,(skyx,0))
				screen.blit(car,(0,570))
				screen.blit(speedDisplay,(53,5))
				screen.blit(kmlabel,rect1)
				screen.blit(speedlabel,rect2)
				screen.blit(whenlabel,rect3)
				pg.display.flip()
		except AttributeError as e:
			print(e)
				
def main():
	GPIOsetup()
	ss = socket_server()

	
if __name__=="__main__":
	main()
	
	
	






