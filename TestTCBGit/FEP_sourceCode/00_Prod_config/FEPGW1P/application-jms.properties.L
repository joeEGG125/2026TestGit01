#IBM Queue
spring.fep.jms.prop.queueManager=QMFEPLOGP01
spring.fep.jms.prop.channel=SVRCONN.TOEMS
spring.fep.jms.prop.connName=10.0.54.33(1415)
spring.fep.jms.prop.user=
spring.fep.jms.prop.password=

#EMS Receiver
spring.fep.jms.ems.receiver.prop.queueManager=QMFEPLOGP01
spring.fep.jms.ems.receiver.prop.channel=SVRCONN.FROMEMS
spring.fep.jms.ems.receiver.prop.connName=10.0.54.33(1415)
spring.fep.jms.ems.receiver.prop.user=
spring.fep.jms.ems.receiver.prop.password=
# Static or RoundRobin
spring.fep.jms.ems.sender.deliveryMode=RoundRobin
#EMS Sender
spring.fep.jms.ems.sender.log[0].queueManager=QMFEPLOGP01
spring.fep.jms.ems.sender.log[0].channel=SVRCONN.TOEMS
spring.fep.jms.ems.sender.log[0].connName=10.0.54.33(1415)
spring.fep.jms.ems.sender.log[0].user=
spring.fep.jms.ems.sender.log[0].password=
spring.fep.jms.ems.sender.log[0].queue-names.ems.destination=FEP.LOG.QL
spring.fep.jms.ems.sender.log[1].queueManager=QMFEPLOGP02
spring.fep.jms.ems.sender.log[1].channel=SVRCONN.TOEMS
spring.fep.jms.ems.sender.log[1].connName=10.0.54.34(1415)
spring.fep.jms.ems.sender.log[1].user=
spring.fep.jms.ems.sender.log[1].password=
spring.fep.jms.ems.sender.log[1].queue-names.ems.destination=FEP.LOG.QL
spring.fep.jms.ems.sender.alert[0].queueManager=QMFEPLOGP01
spring.fep.jms.ems.sender.alert[0].channel=SVRCONN.TOEMS
spring.fep.jms.ems.sender.alert[0].connName=10.0.54.33(1415)
spring.fep.jms.ems.sender.alert[0].user=
spring.fep.jms.ems.sender.alert[0].password=
spring.fep.jms.ems.sender.alert[0].queue-names.ems.destination=FEP.EMS.QL
spring.fep.jms.ems.sender.alert[1].queueManager=QMFEPLOGP02
spring.fep.jms.ems.sender.alert[1].channel=SVRCONN.TOEMS
spring.fep.jms.ems.sender.alert[1].connName=10.0.54.34(1415)
spring.fep.jms.ems.sender.alert[1].user=
spring.fep.jms.ems.sender.alert[1].password=
spring.fep.jms.ems.sender.alert[1].queue-names.ems.destination=FEP.EMS.QL

#Topic Name
spring.fep.jms.topic-names.test.destination=TestTopic

#Queue Name
spring.fep.jms.queue-names.ems.destination=FEP.EMS.QL