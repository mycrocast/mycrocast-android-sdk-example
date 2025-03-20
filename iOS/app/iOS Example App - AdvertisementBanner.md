# iOS Example App - AdvertisementBanner



An Advertisement can have an image configured with a description in addition to just the audio spot. If a banner image is present, there is also a url to a target website. If we click the advertisement we want to move the user to the configured url. In this example we use a "learn more" button.

The advertisement click should be propagated to our system through the Mycrocast.shared.advertisements.onAdBannerClicked() for statistical purpose for your sponsors to see

````swift
import Foundation
import UIKit
import MycrocastSDK

// Example view for showing a banner during an advertisement
// this could be extended to include a progress bar to show how long
// this banner will be shown after we received an advertisement and decided to play it

class AdvertisementView: UIView {

    private let title: UILabel = UILabel()
    private let adName: UILabel = UILabel()
    private let bannerImage: UIImageView = UIImageView()
    private let more: UIButton = UIButton()

    private var advertisement: MycrocastAdvertisement?

    init() {
        super.init(frame: .zero)
        self.createViews()
        self.backgroundColor = Colors.darkBackground
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func createViews() {
        self.title.textAlignment = .center
        self.title.translatesAutoresizingMaskIntoConstraints = false
        self.title.textColor = .white
        self.title.font = .systemFont(ofSize: 12)
        self.title.text = "Advertisement playing please wait"
        self.addSubview(self.title)

        self.title.topAnchor.constraint(equalTo: self.topAnchor, constant: 5).isActive = true
        self.title.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 5).isActive = true
        self.title.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: 5).isActive = true

        self.adName.textAlignment = .center
        self.adName.translatesAutoresizingMaskIntoConstraints = false
        self.adName.textColor = .white
        self.addSubview(self.adName)

        self.adName.leadingAnchor.constraint(equalTo: self.title.leadingAnchor).isActive = true
        self.adName.trailingAnchor.constraint(equalTo: self.title.trailingAnchor).isActive = true
        self.adName.topAnchor.constraint(equalTo: self.title.bottomAnchor, constant: 25).isActive = true

        self.bannerImage.translatesAutoresizingMaskIntoConstraints = false
        self.bannerImage.backgroundColor = .black
        self.addSubview(self.bannerImage)

        self.bannerImage.centerYAnchor.constraint(equalTo: self.centerYAnchor).isActive = true
        self.bannerImage.centerXAnchor.constraint(equalTo: self.centerXAnchor).isActive = true
        self.bannerImage.widthAnchor.constraint(equalTo: self.widthAnchor, multiplier: 0.9).isActive = true
        self.bannerImage.heightAnchor.constraint(equalTo: self.bannerImage.widthAnchor).isActive = true

        self.more.setTitle("More", for: .normal)
        self.more.addTarget(self, action: #selector(self.moreTouched), for: .touchUpInside)
        self.more.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.more)

        self.more.centerXAnchor.constraint(equalTo: self.centerXAnchor).isActive = true
        self.more.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: -5).isActive = true
    }

    /**
     We display the banner and load the bannerFile if any was provided
     - Parameter ad: the advertisement we want to display
     */
    func display(_ ad: MycrocastAdvertisement) {
        self.advertisement = ad;

        self.adName.text = ad.advertisementDescription
        if let url = ad.bannerTargetUrl {
            self.bannerImage.downloaded(from: url)
        } else {
            // a default image could be displayed here
        }
    }

    /**
     The banner advertisement was clicked, therefore we
      inform the sdk about this and also open the provided url
     - Parameter sender:
     */
    @objc private func moreTouched(sender: UIButton) {
        if let advertisement = self.advertisement {
            if let url = URL(string: advertisement.bannerUrl) {
                AppState.shared.audioState.advertisementClicked(advertisement)
                UIApplication.shared.open(url)
            }
        }
    }
}

````

