#pragma once

#include <cursespp/LayoutBase.h>

#include <app/window/CategoryListView.h>
#include <app/window/TrackListView.h>
#include <app/window/TransportWindow.h>
#include <app/service/PlaybackService.h>

#include <core/playback/Transport.h>
#include <core/library/ILibrary.h>

#include <sigslot/sigslot.h>

namespace musik {
    namespace box {
        class BrowseLayout : public cursespp::LayoutBase, public sigslot::has_slots<> {
            public:
                BrowseLayout(
                    PlaybackService& playback,
                    musik::core::LibraryPtr library);

                virtual ~BrowseLayout();

                virtual void Layout();
                virtual void Show();
                virtual cursespp::IWindowPtr GetFocus();
                virtual bool KeyPress(const std::string& key);

            private:
                void InitializeWindows();

                void RequeryTrackList(ListWindow *view);

                void OnCategoryViewSelectionChanged(
                    ListWindow *view, size_t newIndex, size_t oldIndex);

                void OnCategoryViewInvalidated(
                    ListWindow *view, size_t selectedIndex);

                PlaybackService& playback;
                musik::core::LibraryPtr library;
                std::shared_ptr<CategoryListView> categoryList;
                std::shared_ptr<TrackListView> trackList;
                cursespp::IWindowPtr focused;
                cursespp::IWindowPtr parent;
        };
    }
}
