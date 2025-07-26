import 'package:flutter/material.dart';
import 'package:mjpegstreamer/commonProvider.dart';
import 'package:mjpegstreamer/mjpeg_sender.dart';
import 'package:mjpegstreamer/mjpeg_viewer.dart';
import 'package:provider/provider.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => commonProvider(),
      child: BroadcastApp(),
    ),
  );
}

class BroadcastApp extends StatelessWidget {
  const BroadcastApp({super.key});

  // const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '🔥 MJPEG Sender/Viewer',
      theme: ThemeData.dark(),
      home: BroadcastHome(),
    );
  }
}

class BroadcastHome extends StatefulWidget {
  const BroadcastHome({super.key});

  @override
  State<BroadcastHome> createState() => _BroadcastHomeState();
}

class _BroadcastHomeState extends State<BroadcastHome> {
  int _selectedIndex = 0;
  
  final _pages = [
    MjpegSender(),
    MjpegViewer(),
  ];
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_selectedIndex == 0 ? "📤 송출기" : "📺 뷰어"),
      ),
      body: _pages[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: (i) => setState(() => _selectedIndex = i),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.send), label: "송출"),
          BottomNavigationBarItem(icon: Icon(Icons.tv), label: "뷰어"),
        ],
      ),
    );
  }
}


