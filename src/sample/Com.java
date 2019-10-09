package sample;

import jssc.*;

public class Com {
    private SerialPort serialPort;
    private boolean isPortOpened = false;
    private boolean noMode;
    private boolean RTS;
    Main main = new Main();

    public boolean connect(String name) {
        if (name.equals("COM1") || name.equals("COM2")) {//проверяем на правильность имен для ком портов
        } else {
            main.debugInformation("Error wrong name");
            return false;
        }
        serialPort = new SerialPort(name);//создаем порт
        main.debugInformation("Trying open port" + serialPort.getPortName());//сообщаем что пытаемся открыть порт
        try {
            serialPort.openPort();//пытаемся открыть порт
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);//устанавливаем свои характеристики для порта
            //Устанавливаем ивент лисенер и маску
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);/*Устанавливаем маску или список события на которые будет происходить реакция. В данном случае это приход данных в буффер порта*/
            isPortOpened = true;//говорим что открыли порт
            serialPort.setRTS(true);//первоначально устанавливаем RTS
            RTS = true;
            noMode = false;
            main.debugInformation("Port has been opened. ");
            main.getInformation("RTS", serialPort.getPortName());
        } catch (SerialPortException e) {//обрабатываем возможные ошибки
            if (e.getExceptionType().equals("Port busy")) {
                main.debugInformation("Cannot open port: port is busy.");
            } else {
                main.debugInformation("Cannot open port: " + e.getExceptionType());
            }
            return false;
        }
        return true;
    }

    public boolean control(String s) {//выбираем режим управления линиями
        if (!s.equals("RTS") && !s.equals("DTR") && !s.equals("noMode")) {//если имена не совпадают то выходим
            return false;
        }
        if (isPortOpened) {
            try {
                switch (s) {//устанавливаем режимы управления
                    case "noMode": {
                        serialPort.setDTR(false);
                        serialPort.setRTS(false);
                        noMode = true;
                        main.getInformation("default", serialPort.getPortName());
                        return true;
                    }
                    case "RTS": {
                        serialPort.setRTS(true);
                        serialPort.setDTR(false);
                        RTS = true;
                        noMode = false;
                        main.getInformation("RTS", serialPort.getPortName());
                        return true;
                    }
                    case "DTR": {
                        serialPort.setDTR(true);
                        serialPort.setRTS(false);
                        noMode = false;
                        RTS = false;
                        main.getInformation("DTR", serialPort.getPortName());
                        return true;
                    }
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
        main.debugInformation("Port not yet activated");
        return false;
    }

    public boolean activateSend(String s) {//активируем отправлние сообщения
        if (!isPortOpened) {
            return false;
        }
        PortReader portReader = new PortReader();
        portReader.send(s);
        return true;
    }

    public class PortReader implements SerialPortEventListener {//класс для чтения
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {//если пришол знак их количество больше 0 то
                try {
                    while (noMode) {
                        if (!serialPort.isCTS() && !serialPort.isDSR()) {
                            byte[] bytes = serialPort.readBytes(1);
                            String data = new String(bytes);
                            main.output(data);
                        }
                    }
                    while (serialPort.isCTS()) {
                        if (RTS && !noMode) {
                            byte[] bytes = serialPort.readBytes(1);
                            String data = new String(bytes);
                            main.output(data);
                        }
                    }
                    while (serialPort.isDSR()) {
                        if (!RTS && !noMode) {
                            byte[] bytes = serialPort.readBytes(1);
                            String data = new String(bytes);
                            main.output(data);
                        }
                    }
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String s) {
            try {
                String str = "$" + s;
                byte[] buffer;
                buffer = str.getBytes();
                for (int i = 0; i < buffer.length; i++) {
                    serialPort.writeByte(buffer[i]);
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}