import 'package:flutter/foundation.dart';

class commonProvider with ChangeNotifier {
  String _url = 'ws://192.168.0.102:8090/ws';
  bool _isStreaming = false;
  bool _isBusy = false;

  String get url => _url;
  bool get isStreaming => _isStreaming;
  bool get isBusy => _isBusy;

  void setUrl(String newUrl) {
    _url = newUrl;
    notifyListeners();
  }
  void setIsStreaming(bool newIsStreaming) {
    _isStreaming = newIsStreaming;
    notifyListeners();
  }
  void setIsBusy(bool newIsBusy) {
    _isBusy = newIsBusy;
    notifyListeners();
  }

}