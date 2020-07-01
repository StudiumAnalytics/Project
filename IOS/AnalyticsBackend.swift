//
//  AnalyticsBackend.swift
//  Studium
//
//  Created by Simone Scionti on 02/06/2020.
//  Copyright © 2020 Unict.it. All rights reserved.
//

import Foundation

public class AnalyticsBackend{
    static var obj : AnalyticsBackend!
    let baseURL = "http://192.168.1.197:5001/api/StudiumAnalytics/"
    private init(){}
    
    public static func getUniqueInstance() -> AnalyticsBackend{
        if obj == nil {
            obj = AnalyticsBackend()
        }
        return obj
    }
    
    
    public func teachingDidLoad(fromTeaching teaching : Teaching){
        let parameters: [String: Any] = [
            "dbName" : teaching.dbName ?? "",
            "teachingName": teaching.name ?? "",
            "tutorName": teaching.tutorName ?? "",
            "userId": Student.getUniqueIstance().id ?? ""
        ]
        if let request = buildRequest(withRequestName: "TeachingDidLoad", withBodyParameters: parameters){
            launchRequest(withRequest: request) { (response) in
                if let JSONResponse = response{
                    print(JSONResponse)
                }
            }
        }
    }
    
    public func sectionDidLoad(fromTeaching teaching : Teaching, withSectionName sectionName : String){
        let teachingDict : [String : Any] = [
            "dbName" : teaching.dbName ?? "",
            "teachingName": teaching.name ?? "",
            "tutorName": teaching.tutorName ?? "",
            "userId": Student.getUniqueIstance().id ?? ""
        ]
        let parameters: [String: Any] = [
            "teaching" : teachingDict,
            "sectionName": sectionName
        ]
        if let request = buildRequest(withRequestName: "SectionDidLoad", withBodyParameters: parameters){
            launchRequest(withRequest: request) { (response) in
                if let JSONResponse = response{
                    print(JSONResponse)
                }
            }
        }
    }
    
    public func teachingStateRefresh(fromTeaching teaching : Teaching){
        let teachingDict : [String : Any] = [
            "dbName" : teaching.dbName ?? "",
            "teachingName": teaching.name ?? "",
            "tutorName": teaching.tutorName ?? "",
            "userId": Student.getUniqueIstance().id ?? ""
        ]
        let parameters: [String: Any] = [
            "teaching" : teachingDict,
            "showCaseExists" : teaching.showcaseHTML != nil && !teaching.showcaseHTML.isEmpty ? true : false,
            "notifiesExists": teaching.notifyList.isEmpty ? false : true,
            "descriptionExists": teaching.descriptionBlocks.count > 0 ? true : false,
            "documentsExists": teaching.fs.currentFolder.childs.count > 0 ? true : false,
            "bookingExists": teaching.haveBooking ?? false
        ]
        if let request = buildRequest(withRequestName: "TeachingStateRefresh", withBodyParameters: parameters){
            launchRequest(withRequest: request) { (response) in
                if let JSONResponse = response{
                    print(JSONResponse)
                }
            }
        }
    }
    
    public func documentDidOpen(fromTeaching teaching : Teaching, document : Doc){
        let teachingDict : [String : Any] = [
            "dbName" : teaching.dbName ?? "",
            "teachingName": teaching.name ?? "",
            "tutorName": teaching.tutorName ?? "",
            "userId": Student.getUniqueIstance().id ?? ""
        ]
        let parameters: [String: Any] = [
            "teaching" : teachingDict,
            "date": document.uploaded ?? "",
            "title": document.title ?? "",
        ]
        if let request = buildRequest(withRequestName: "DocumentDidOpen", withBodyParameters: parameters){
            launchRequest(withRequest: request) { (response) in
                if let JSONResponse = response{
                    print(JSONResponse)
                }
            }
        }
    }
    public func notificationDidOpen(fromTeaching teaching : Teaching, notification : Notify){
           let teachingDict : [String : Any] = [
               "dbName" : teaching.dbName ?? "",
               "teachingName": teaching.name ?? "",
               "tutorName": teaching.tutorName ?? "",
               "userId": Student.getUniqueIstance().id ?? ""
           ]
           let parameters: [String: Any] = [
               "teaching" : teachingDict,
               "date": notification.date ?? "",
               "title": notification.title ?? notification.itemTitle ?? "",
           ]
           if let request = buildRequest(withRequestName: "NotificationDidOpen", withBodyParameters: parameters){
               launchRequest(withRequest: request) { (response) in
                   if let JSONResponse = response{
                       print(JSONResponse)
                   }
               }
           }
       }
    
    
    private func buildRequest(withRequestName requestName: String , withBodyParameters parameters : [String:Any] ) -> URLRequest? {
        
        let Url = String(format: baseURL + requestName)
        guard let serviceUrl = URL(string: Url) else { return nil }
        var request = URLRequest(url: serviceUrl)
        request.httpMethod = "POST"
        request.setValue("Application/json", forHTTPHeaderField: "Content-Type")
        guard let httpBody = try? JSONSerialization.data(withJSONObject: parameters, options: []) else {
            return nil
        }
        request.httpBody = httpBody
        request.timeoutInterval = 20
        
        return request
    }
        
        
    private func launchRequest(withRequest request : URLRequest, completion: @escaping (Any?) ->()){
       
        let session = URLSession.shared
        session.dataTask(with: request) { (data, response, error) in
            if let response = response {
                print(response)
            }
            if let data = data {
                do {
                    let json = try JSONSerialization.jsonObject(with: data, options: [])
                    completion(json)
                } catch {
                    completion(nil)
                    print(error)
                    
                }
            }
        }.resume()
    }
}

