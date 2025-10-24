import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class PcapAnalyzer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java PcapAnalyzer <pcap-file>");
            return;
        }
        
        String pcapFile = args[0];
        
        try {
            PcapHandle handle = Pcaps.openOffline(pcapFile);
            
            System.out.println("Analyzing PCAP file: " + pcapFile);
            System.out.println("==========================================\n");
            
            int packetCount = 0;
            
            while (true) {
                try {
                    Packet packet = handle.getNextPacketEx();
                    packetCount++;
                    
                    System.out.println("Packet #" + packetCount);
                    System.out.println("Length: " + packet.length() + " bytes");
                    
                    // Check for TCP packet
                    if (packet.contains(TcpPacket.class)) {
                        TcpPacket tcpPacket = packet.get(TcpPacket.class);
                        TcpPacket.TcpHeader tcpHeader = tcpPacket.getHeader();
                        
                        System.out.println("Protocol: TCP");
                        System.out.println("Source Port: " + tcpHeader.getSrcPort());
                        System.out.println("Destination Port: " + tcpHeader.getDstPort());
                        
                        // Extract payload
                        Packet payload = tcpPacket.getPayload();
                        if (payload != null) {
                            byte[] data = payload.getRawData();
                            if (data != null && data.length > 0) {
                                System.out.println("\n--- PAYLOAD (Plain Text) ---");
                                String plainText = new String(data);
                                System.out.println(plainText);
                                System.out.println("--- END PAYLOAD ---");
                                
                                // Also show hex dump
                                System.out.println("\n--- HEX DUMP ---");
                                printHexDump(data);
                                System.out.println("--- END HEX DUMP ---");
                            }
                        }
                    }
                    
                    // Check for IP packet
                    if (packet.contains(IpV4Packet.class)) {
                        IpV4Packet ipPacket = packet.get(IpV4Packet.class);
                        IpV4Packet.IpV4Header ipHeader = ipPacket.getHeader();
                        
                        System.out.println("Source IP: " + ipHeader.getSrcAddr());
                        System.out.println("Destination IP: " + ipHeader.getDstAddr());
                    }
                    
                    System.out.println("==========================================\n");
                    
                } catch (TimeoutException e) {
                    // No more packets
                    break;
                } catch (EOFException e) {
                    // End of file
                    break;
                }
            }
            
            System.out.println("Total packets analyzed: " + packetCount);
            handle.close();
            
        } catch (PcapNativeException | NotOpenException e) {
            System.err.println("Error reading PCAP file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printHexDump(byte[] data) {
        int bytesPerLine = 16;
        for (int i = 0; i < data.length; i += bytesPerLine) {
            // Print offset
            System.out.printf("%04X: ", i);
            
            // Print hex values
            for (int j = 0; j < bytesPerLine; j++) {
                if (i + j < data.length) {
                    System.out.printf("%02X ", data[i + j]);
                } else {
                    System.out.print("   ");
                }
            }
            
            System.out.print(" | ");
            
            // Print ASCII representation
            for (int j = 0; j < bytesPerLine && i + j < data.length; j++) {
                byte b = data[i + j];
                if (b >= 32 && b <= 126) {
                    System.out.print((char) b);
                } else {
                    System.out.print(".");
                }
            }
            
            System.out.println();
        }
    }
}