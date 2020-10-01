#include "LimitKeeper.h"

#include <algorithm>
#include <tuple>

using namespace Utils;

Limits LimitKeeper::getLimits() const noexcept
{
    return { tops.empty() ? 0 : tops.begin()->first,
        rights.empty() ? 0 : std::prev(rights.end())->first,
        bottoms.empty() ? 0 : std::prev(bottoms.end())->first,
        lefts.empty() ? 0 : lefts.begin()->first };
}

Limits LimitKeeper::tryLimits(const WordParams& params, std::size_t size) const noexcept
{
    auto tryL = [&, this](int right, int bottom)
    {
        return Limits{ std::min(tops.empty() ? 0 : tops.begin()->first, params.start.y),
            std::max(rights.empty() ? 0 : std::prev(rights.end())->first, right),
            std::max(bottoms.empty() ? 0 : std::prev(bottoms.end())->first, bottom),
            std::min(lefts.empty() ? 0 : lefts.begin()->first, params.start.x) };
    };
    auto size_ = static_cast<int>(size);
    return params.orientation == WordOrientation::HORIZONTAL ?
    tryL(params.start.x + size_, params.start.y + 1)
        : tryL(params.start.x + 1, params.start.y + size_);
}

void LimitKeeper::apply(const std::function<void(int, int)>& f,
        const WordParams& params, std::size_t size)
{
    auto size_ = static_cast<int>(size);
    if (params.orientation == WordOrientation::HORIZONTAL)
    {
        f(params.start.x + size_, params.start.y + 1);
    }
    else f(params.start.x + 1, params.start.y + size_);
}

void LimitKeeper::addWord(const WordParams& params, std::size_t size) noexcept
{
    auto add = [&, this](int right, int bottom)
    {
        const auto top = params.start.y;
        if (auto it = tops.find(top); it == tops.end()) tops.emplace(top, 1);
        else ++it->second;
        if (auto it = rights.find(right); it == rights.end()) rights.emplace(right, 1);
        else ++it->second;
        if (auto it = bottoms.find(bottom); it == bottoms.end()) bottoms.emplace(bottom, 1);
        else ++it->second;
        const auto left = params.start.x;
        if (auto it = lefts.find(left); it == lefts.end()) lefts.emplace(left, 1);
        else ++it->second;
    };
    apply(add, params, size);
}

void LimitKeeper::removeWord(const Utils::WordParams& params, std::size_t size)
{
    auto remove = [&, this](int right, int bottom)
    {
        const auto top = params.start.y, left = params.start.x;
        using directiomParams = std::tuple<std::map<int, std::size_t>::iterator,
        std::map<int, std::size_t>&, std::string>;
        std::array params{ directiomParams{tops.find(top), tops,
                                           "This top limit doesn't exist"},
        directiomParams{rights.find(right), rights,  "This right limit doesn't exist"},
        directiomParams{bottoms.find(bottom), bottoms,
                        "This bottom limit doesn't exist"},
        directiomParams{lefts.find(left), lefts,  "This left limit doesn't exist"} };
        for (auto& [it, m, message] : params) if (it == m.end())
        {
            [[unlikely]] throw std::runtime_error{ message};
        }
        for (auto& [it, m, message] : params) if (!--it->second) m.erase(it);
    };
    apply(remove, params, size);
}
